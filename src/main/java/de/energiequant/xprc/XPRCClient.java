package de.energiequant.xprc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.energiequant.xprc.commands.CommandBuilderFactory;

public class XPRCClient implements Closeable, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(XPRCClient.class);

    private final String logPrefix;
    private final ConnectionParameters connectionParameters;
    private final Consumer<XPRCException> exceptionHandler;
    private final Collection<SessionMonitor> sessionMonitors;
    private final Duration channelBlockDuration;

    private final Thread connectSendThread;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final AtomicReference<Session> session = new AtomicReference<>();

    private Socket socket;
    private BufferedWriter bw;
    private BufferedReader br;

    private static final Duration SLEEP_CHECK_INTERVAL = Duration.ofSeconds(1);
    private static final Duration CONNECT_WAIT_CHECK_INTERVAL = Duration.ofMillis(200);

    private static final Duration SOCKET_TIMEOUT = Duration.ofSeconds(5);
    private static final String HANDSHAKE_PREFIX = "XPRC;";
    private static final int PROTOCOL_VERSION = 1;

    private static final Duration THREAD_JOIN_TIMEOUT = Duration.ofSeconds(30);

    XPRCClient(ConnectionParameters connectionParameters, Consumer<XPRCException> exceptionHandler, Collection<SessionMonitor> sessionMonitors, Duration channelBlockDuration) {
        this.logPrefix = "[" + connectionParameters.getAlias() + "] [main] ";
        this.connectionParameters = connectionParameters;
        this.exceptionHandler = exceptionHandler;
        this.sessionMonitors = new ArrayList<>(sessionMonitors);
        this.channelBlockDuration = channelBlockDuration;

        connectSendThread = new Thread(this::runConnectSendThread);
        connectSendThread.setName(XPRCClient.class.getSimpleName() + " " + connectionParameters.getAlias() + " send/connect");
        connectSendThread.start();
    }

    public ConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }

    private void runConnectSendThread() {
        LOGGER.debug("{}connect/send thread started", logPrefix);

        while (!shutdown.get()) {
            try {
                LOGGER.debug("{}connecting", logPrefix);
                Session session = connect();
                if (session == null) {
                    LOGGER.debug("{}connection failed", logPrefix);
                } else {
                    LOGGER.debug("{}connected, using thread to send", logPrefix);
                    this.session.set(session);
                    try {
                        session.runSendThread();
                    } finally {
                        session.close();
                        this.session.compareAndSet(session, null);
                    }

                    LOGGER.debug("{}sender terminated, closing socket", logPrefix);
                    closeSocket();
                }
            } catch (Exception ex) {
                LOGGER.warn("{}error while connected", logPrefix, ex);
                if (ex instanceof XPRCException) {
                    XPRCException xprcException = (XPRCException) ex;
                    if (xprcException.getConsequence() != XPRCException.Consequence.RECONNECT) {
                        shutdown.set(true);
                    }

                    xprcException.sendTo(exceptionHandler);
                } else {
                    XPRCException.sendTo(exceptionHandler, this, XPRCException.Consequence.UNRECOVERABLE, "", ex);
                    shutdown.set(true);
                }

                closeSocket();
            }

            LOGGER.debug("{}waiting to reconnect", logPrefix);
            boolean sleepComplete = sleepFor(connectionParameters.getReconnectDelay(), SLEEP_CHECK_INTERVAL, shutdown::get);
            if (!sleepComplete) {
                LOGGER.warn("{}reconnect timer interrupted, shutting down", logPrefix);
                break;
            }
        }

        LOGGER.debug("{}stopping", logPrefix);
        shutdown.set(true);
        // FIXME: close any open session

        LOGGER.debug("{}stopped", logPrefix);
    }

    Consumer<XPRCException> getExceptionHandler() {
        return exceptionHandler;
    }

    public Optional<Session> getSession() {
        return Optional.ofNullable(session.get());
    }

    public boolean isConnected() {
        if (shutdown.get()) {
            return false;
        }

        return getSession().map(Session::isOpen).orElse(false);
    }

    public boolean waitUntilConnected(Duration timeout) {
        sleepFor(timeout, CONNECT_WAIT_CHECK_INTERVAL, () -> shutdown.get() || isConnected());
        return isConnected();
    }

    private boolean sleepFor(Duration sleepDuration, Duration checkInterval, BooleanSupplier breakCondition) {
        long checkIntervalMillis = checkInterval.toMillis();

        Instant endOfSleep = Instant.now().plus(sleepDuration);
        long millisUntilComplete = Duration.between(Instant.now(), endOfSleep).toMillis();
        while ((millisUntilComplete > 0) && !breakCondition.getAsBoolean()) {
            try {
                Thread.sleep(Math.min(millisUntilComplete, checkIntervalMillis));
            } catch (InterruptedException ex) {
                LOGGER.warn("{}Sleep interrupted", logPrefix, ex);
                return false;
            }

            millisUntilComplete = Duration.between(Instant.now(), endOfSleep).toMillis();
        }

        return millisUntilComplete <= 0;
    }

    /**
     * Provides easy access to command builders.
     * <p>
     * Recommended to only be called after the connection has been successfully established.
     * See {@link CommandBuilderFactory} for details.
     * </p>
     *
     * @return factory to create command builders
     */
    public CommandBuilderFactory commandBuilders() {
        return new CommandBuilderFactory(this);
    }

    public <CFB extends ChannelFactoryBuilder<CFB, CH, C, M>, CH extends Channel<CH, C, M>, C extends Command<CFB, CH, C, M>, M extends ChannelMessage> CFB prepareChannel(Command.Builder<?, C, CH, CFB, M, ?> commandBuilder) {
        // TODO: does it make sense to keep the builder and instantiate only when actually submitting?
        return prepareChannel(commandBuilder.build());
    }

    public <CFB extends ChannelFactoryBuilder<CFB, CH, C, M>, CH extends Channel<CH, C, M>, C extends Command<CFB, CH, C, M>, M extends ChannelMessage> CFB prepareChannel(C command) {
        return command.createChannelFactoryBuilder(this);
    }

    public <CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> Optional<CH> submitCommand(ChannelFactoryBuilder<?, CH, C, M> channelFactoryBuilder) {
        Session sessionAtTimeOfCall = session.get();
        if (sessionAtTimeOfCall == null) {
            return Optional.empty();
        }

        //session.

        return Optional.empty();
    }

    private Session connect() {
        socket = new Socket();
        br = null;
        bw = null;

        List<char[]> handshakeInformation = new ArrayList<>();
        try {
            try {
                socket.connect(
                    new InetSocketAddress(
                        connectionParameters.getHost(),
                        connectionParameters.getPort()
                    ),
                    (int) connectionParameters.getConnectTimeout().toMillis()
                );
            } catch (SocketTimeoutException ex) {
                throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "timeout trying to connect", ex);
            } catch (Exception ex) {
                throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "exception while trying to connect", ex);
            }

            try {
                socket.setKeepAlive(true);
            } catch (SocketException ex) {
                LOGGER.warn("{}Failed to enable keep-alives", logPrefix, ex);
            }

            try {
                socket.setTcpNoDelay(true);
            } catch (SocketException ex) {
                LOGGER.warn("{}Failed to enable 'no delay'", logPrefix, ex);
            }

            try {
                socket.setSoTimeout((int) SOCKET_TIMEOUT.toMillis());
            } catch (SocketException ex) {
                LOGGER.warn("{}Failed to set socket timeout of {}", logPrefix, SOCKET_TIMEOUT, ex);
            }

            br = createBufferedReader(socket);
            bw = createBufferedWriter(socket);

            String requestLine;
            try {
                requestLine = br.readLine();
            } catch (Exception ex) {
                throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "Failed to read handshake request line", ex);
            }

            if (requestLine == null) {
                throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Server closed connection instead of sending handshake request line; server probably does not run XPRC");
            }

            LOGGER.debug("{}Received handshake request: \"{}\"", logPrefix, requestLine);

            if (!requestLine.startsWith(HANDSHAKE_PREFIX)) {
                throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Unexpected initial line received: \"" + requestLine + "\"");
            }

            for (String requestedInformation : requestLine.substring(HANDSHAKE_PREFIX.length()).split(",")) {
                if ("version".equals(requestedInformation)) {
                    LOGGER.debug("{}server requests protocol version => {}", logPrefix, PROTOCOL_VERSION);
                    handshakeInformation.add(("v" + PROTOCOL_VERSION).toCharArray());
                } else if ("password".equals(requestedInformation)) {
                    LOGGER.debug("{}server requests password", logPrefix);
                    try {
                        handshakeInformation.add(connectionParameters.getPasswordProvider().get());
                    } catch (Exception ex) {
                        throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "Failed to retrieve password", ex);
                    }
                } else {
                    LOGGER.warn("{}server requests unsupported handshake information \"{}\"", logPrefix, requestedInformation);
                    throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Server requests unsupported handshake information \"" + requestedInformation + "\"");
                }
            }

            if (handshakeInformation.isEmpty()) {
                LOGGER.warn("{}server did not request any information during handshake", logPrefix);
            }

            try {
                for (char[] chars : handshakeInformation) {
                    bw.write(chars);
                    bw.write('\n');
                }
                bw.flush();
            } catch (IOException ex) {
                throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "Failed to send handshake response", ex);
            }

            String confirmationLine;
            Instant localReferenceTimestamp;
            try {
                confirmationLine = br.readLine();
                localReferenceTimestamp = Instant.now();
            } catch (Exception ex) {
                throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "Failed to read handshake confirmation line", ex);
            }

            if (confirmationLine == null) {
                throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "Handshake was not confirmed, password may be wrong");
            }

            LOGGER.debug("{}Received handshake confirmation: \"{}\"", logPrefix, confirmationLine);

            int endOfVersion = confirmationLine.indexOf(';');
            if (endOfVersion < 0) {
                throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Unexpected handshake response line received (no version delimiter): \"" + confirmationLine + "\"");
            } else if (endOfVersion == confirmationLine.length() - 1) {
                throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Unexpected handshake response line received (premature end of line): \"" + confirmationLine + "\"");
            }

            String status = confirmationLine.substring(endOfVersion + 1);
            if (status.startsWith("ERR:")) {
                // this is not a password mismatch (server would just close the connection without talking to us)
                // but some other issue we probably cannot hope to be resolved on retry
                throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Handshake denied by server: \"" + status.substring(4) + "\"");
            } else if (!status.startsWith("OK;")) {
                throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Unexpected handshake response line received (invalid status code): \"" + confirmationLine + "\"");
            }

            String remoteReferenceTimestampString = status.substring(3);
            Instant remoteReferenceTimestamp;
            try {
                remoteReferenceTimestamp = Instant.parse(remoteReferenceTimestampString);
            } catch (Exception ex) {
                throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Incompatible timestamp format received during handshake: \"" + remoteReferenceTimestampString + "\"");
            }

            return new Session(
                this,
                localReferenceTimestamp,
                remoteReferenceTimestamp,
                new ChannelPool(
                    "[" + connectionParameters.getAlias() + "] ",
                    channelBlockDuration
                ),
                sessionMonitors
            );
        } catch (Exception ex) {
            LOGGER.warn("{}connection failed", logPrefix, ex);
            if (ex instanceof XPRCException) {
                XPRCException xprcException = (XPRCException) ex;
                if (xprcException.getConsequence() != XPRCException.Consequence.RECONNECT) {
                    shutdown.set(true);
                }

                xprcException.sendTo(exceptionHandler);
            } else {
                XPRCException.sendTo(exceptionHandler, this, XPRCException.Consequence.UNRECOVERABLE, "", ex);
                shutdown.set(true);
            }

            closeSocket();

            return null;
        } finally {
            // handshake information may hold private data, clear before disposal
            for (char[] chars : handshakeInformation) {
                Arrays.fill(chars, (char) 0);
            }
            handshakeInformation.clear();
        }
    }

    private void closeSocket() {
        if (br != null) {
            try {
                br.close();
            } catch (IOException ex) {
                LOGGER.warn("{}Failed to close reader", logPrefix, ex);
            }
        }
        br = null;

        if (bw != null) {
            try {
                bw.close();
            } catch (IOException ex) {
                LOGGER.warn("{}Failed to close writer", logPrefix, ex);
            }
        }
        bw = null;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                LOGGER.warn("{}Failed to close socket", logPrefix, ex);
            }
        }
        socket = null;
    }

    private BufferedReader createBufferedReader(Socket socket) {
        InputStream is;
        try {
            is = socket.getInputStream();
        } catch (IOException ex) {
            throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "Failed to get InputStream from socket", ex);
        }

        return new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));
    }

    private BufferedWriter createBufferedWriter(Socket socket) {
        OutputStream os;
        try {
            os = socket.getOutputStream();
        } catch (IOException ex) {
            throw new XPRCException(this, XPRCException.Consequence.RECONNECT, "Failed to get OutputStream from socket", ex);
        }

        return new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.US_ASCII));
    }

    Socket getSocket() {
        return socket;
    }

    BufferedWriter getBufferedWriter() {
        return bw;
    }

    BufferedReader getBufferedReader() {
        return br;
    }

    @Override
    public void close() {
        shutdown.set(true);
        getSession().ifPresent(Session::close);
        closeSocket();

        try {
            connectSendThread.join(THREAD_JOIN_TIMEOUT.toMillis());
        } catch (InterruptedException ex) {
            throw new XPRCException(this, XPRCException.Consequence.UNRECOVERABLE, "Interrupted while waiting to join connect/send thread", ex);
        }

        if (connectSendThread.isAlive()) {
            throw new XPRCException(this, XPRCException.Consequence.RESOURCE_LEAK, "Connect/send thread could not be joined within timeout");
        }
    }

    @Override
    public String toString() {
        return "XPRCClient(" + connectionParameters + ")";
    }
}
