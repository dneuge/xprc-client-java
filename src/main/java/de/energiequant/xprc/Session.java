package de.energiequant.xprc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.energiequant.xprc.SessionMonitor.Direction;
import de.energiequant.xprc.XPRCException.Consequence;
import de.energiequant.xprc.utils.ImmutablePair;

public class Session implements AutoCloseable, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    private final XPRCClient client;
    private final Socket socket;
    private final BufferedReader br;
    private final BufferedWriter bw;
    private final Instant localReferenceTimestamp;
    private final Instant remoteReferenceTimestamp;
    private final ChannelPool channelPool;
    private final Collection<SessionMonitor> sessionMonitors;
    private final String logPrefix;

    private final Deque<ImmutablePair<Channel<?, ?, ?>, String>> outboundQueue = new ArrayDeque<>();
    private final Deque<ImmutablePair<Instant, String>> inboundQueue = new ArrayDeque<>();
    private final AtomicBoolean shouldShutdown = new AtomicBoolean(false);

    private final Map<ChannelId, Channel<?, ?, ?>> channels = new HashMap<>();

    private final Thread receiveThread;
    private final Thread processingThread;

    private static final Duration SEND_QUEUE_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration INBOUND_QUEUE_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration THREAD_JOIN_TIMEOUT = Duration.ofSeconds(20);

    Session(XPRCClient client, Instant localReferenceTimestamp, Instant remoteReferenceTimestamp, ChannelPool channelPool, Collection<SessionMonitor> sessionMonitors) {
        this.logPrefix = "[" + client.getConnectionParameters().getAlias() + "] ";
        this.client = client;
        this.localReferenceTimestamp = localReferenceTimestamp;
        this.remoteReferenceTimestamp = remoteReferenceTimestamp;
        this.channelPool = channelPool;
        this.sessionMonitors = new ArrayList<>(sessionMonitors);

        this.socket = client.getSocket();
        this.br = client.getBufferedReader();
        this.bw = client.getBufferedWriter();

        receiveThread = new Thread(this::runReceiveThread);
        receiveThread.setName(XPRCClient.class.getSimpleName() + " " + client.getConnectionParameters().getAlias() + " receive");
        receiveThread.start();

        processingThread = new Thread(this::runProcessingThread);
        processingThread.setName(XPRCClient.class.getSimpleName() + " " + client.getConnectionParameters().getAlias() + " processing");
        processingThread.start();
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public void terminate(ChannelId id) {
        // FIXME: implement
    }

    public boolean isOpen() {
        return !shouldShutdown.get();
    }

    public XPRCClient getClient() {
        return client;
    }

    private void notifyMonitorsAboutSession(BiConsumer<SessionMonitor, Session> callback) {
        for (SessionMonitor sessionMonitor : sessionMonitors) {
            try {
                callback.accept(sessionMonitor, this);
            } catch (Exception ex) {
                LOGGER.warn("{}Failed to notify SessionMonitor {} about session", logPrefix, sessionMonitor, ex);
            }
        }
    }

    private void notifyMonitorsAboutChannel(BiConsumer<SessionMonitor, Channel<?, ?, ?>> callback, Channel<?, ?, ?> channel) {
        for (SessionMonitor sessionMonitor : sessionMonitors) {
            try {
                callback.accept(sessionMonitor, channel);
            } catch (Exception ex) {
                LOGGER.warn("{}Failed to notify SessionMonitor {} about channel {}", logPrefix, sessionMonitor, channel, ex);
            }
        }
    }

    private void notifyMonitorsAboutRawMessage(Direction direction, String msg) {
        for (SessionMonitor sessionMonitor : sessionMonitors) {
            try {
                sessionMonitor.onRawMessage(this, direction, msg);
            } catch (Exception ex) {
                LOGGER.warn("{}Failed to notify SessionMonitor {} about {} message \"{}\"", logPrefix, sessionMonitor, direction, msg, ex);
            }
        }
    }

    @Override
    public void close() {
        shutdown();

        Throwable interruption = null;

        try {
            processingThread.join(THREAD_JOIN_TIMEOUT.toMillis());
        } catch (InterruptedException ex) {
            XPRCException.sendTo(client.getExceptionHandler(), client, Consequence.UNRECOVERABLE, "Interrupted while joining processing thread", ex);
            interruption = ex;
        }

        try {
            receiveThread.join(THREAD_JOIN_TIMEOUT.toMillis());
        } catch (InterruptedException ex) {
            XPRCException.sendTo(client.getExceptionHandler(), client, Consequence.UNRECOVERABLE, "Interrupted while joining receive thread", ex);
            interruption = ex;
        }

        if (interruption != null) {
            throw new XPRCException(client, Consequence.UNRECOVERABLE, "Interrupted while waiting to join threads", interruption);
        }

        if (processingThread.isAlive()) {
            throw new XPRCException(client, Consequence.RESOURCE_LEAK, "Processing thread could not be joined within timeout");
        }

        if (receiveThread.isAlive()) {
            throw new XPRCException(client, Consequence.RESOURCE_LEAK, "Receive thread could not be joined within timeout");
        }
    }

    private void shutdown() {
        if (!shouldShutdown.compareAndSet(false, true)) {
            // already shutting down
            return;
        }

        synchronized (outboundQueue) {
            outboundQueue.notifyAll();
        }

        synchronized (inboundQueue) {
            inboundQueue.notifyAll();
        }

        notifyMonitorsAboutSession(SessionMonitor::onSessionClosed);
    }

    private void runReceiveThread() {
        String recvLogPrefix = logPrefix + "[recv] ";
        LOGGER.debug("{}starting", recvLogPrefix);

        boolean shouldStop = false;

        Deque<ImmutablePair<Instant, String>> received = new ArrayDeque<>();
        try {
            while (!(shouldShutdown.get() || shouldStop)) {
                received.clear();

                // first line per iteration should block (we want to be notified immediately when a new line is available)
                ImmutablePair<Instant, String> line;
                try {
                    line = receiveLine();
                } catch (SocketTimeoutException ex) {
                    // intended side effect of having a socket timeout set; we wanted to be woken up at regular
                    // intervals, so this is what we get - just ignore and check loop conditions
                    continue;
                }

                if (line == null) {
                    LOGGER.debug("{}connection terminated", recvLogPrefix);
                    break;
                }
                received.addLast(line);
                notifyMonitorsAboutRawMessage(Direction.SERVER_TO_CLIENT, line.getRight());

                // we continue reading all available lines immediately to avoid synchronization overhead
                while (br.ready()) {
                    try {
                        line = receiveLine();
                    } catch (IOException ex) {
                        // this can happen if the stream was closed in the mean-time or we had some issue decoding the
                        // message string
                        // we still want to record all previously received messages, though
                        shouldStop = true;
                        XPRCException.sendTo(client.getExceptionHandler(), client, Consequence.RECONNECT, "Failed to receive message (continuation)", ex);
                        break;
                    }

                    if (line == null) {
                        LOGGER.debug("{}connection terminated (continuation)", recvLogPrefix);
                        shouldStop = true;
                        break;
                    }

                    received.addLast(line);
                    notifyMonitorsAboutRawMessage(Direction.SERVER_TO_CLIENT, line.getRight());
                }

                synchronized (inboundQueue) {
                    inboundQueue.addAll(received);
                    inboundQueue.notifyAll();
                }
            }
        } catch (IOException ex) {
            if (shouldShutdown.get()) {
                LOGGER.debug("{}Connection terminated while shutting down (expected)", recvLogPrefix, ex);
            } else {
                XPRCException.sendTo(client.getExceptionHandler(), client, Consequence.RECONNECT, "Failed to receive message", ex);
            }
        } catch (Exception ex) {
            LOGGER.warn("{}Unhandled exception", recvLogPrefix, ex);
            XPRCException.sendTo(client.getExceptionHandler(), client, Consequence.RECONNECT, "Unhandled exception in receive thread", ex);
        }

        LOGGER.debug("{}stopping", recvLogPrefix);
        shutdown();
    }

    private ImmutablePair<Instant, String> receiveLine() throws IOException {
        String line = br.readLine();
        Instant timestamp = Instant.now();

        if (line == null) {
            return null;
        }

        return ImmutablePair.of(timestamp, line);
    }

    void runSendThread() {
        String sendLogPrefix = logPrefix + "[send] ";
        LOGGER.debug("{}starting", sendLogPrefix);

        // callback may depend on this thread continuing so we need to notify using a separate thread
        // TODO: check for a better way to notify without blocking the send thread (callback may depend on flushing send queue/receiveing responses to commands)
        Thread connectNotificationThread = new Thread(() -> notifyMonitorsAboutSession(SessionMonitor::onConnected));
        connectNotificationThread.setName(XPRCClient.class.getSimpleName() + " " + client.getConnectionParameters().getAlias() + " onConnected");
        connectNotificationThread.start();

        long sendQueueTimeoutMillis = SEND_QUEUE_TIMEOUT.toMillis();

        try {
            while (!shouldShutdown.get()) {
                List<ImmutablePair<Channel<?, ?, ?>, String>> messages = null;

                synchronized (outboundQueue) {
                    if (!outboundQueue.isEmpty()) {
                        messages = new ArrayList<>(outboundQueue);
                        outboundQueue.clear();
                    }
                }

                if (messages != null) {
                    for (ImmutablePair<Channel<?, ?, ?>, String> msg : messages) {
                        Channel<?, ?, ?> channel = msg.getLeft();
                        if (channel != null) {
                            channel.onDispatch();
                        }

                        String s = msg.getRight();
                        notifyMonitorsAboutRawMessage(Direction.CLIENT_TO_SERVER, s);

                        bw.write(s);
                        bw.write('\n');

                        notifyMonitorsAboutChannel(SessionMonitor::onDispatched, channel);
                    }
                    bw.flush();
                }

                synchronized (outboundQueue) {
                    if (outboundQueue.isEmpty() && !shouldShutdown.get()) {
                        LOGGER.debug("{}waiting for data to send", sendLogPrefix); // DEBUG
                        outboundQueue.wait(sendQueueTimeoutMillis);
                    }
                }
            }
        } catch (IOException ex) {
            throw new XPRCException(client, Consequence.RECONNECT, "Failed to send queued message", ex);
        } catch (InterruptedException ex) {
            throw new XPRCException(client, Consequence.UNRECOVERABLE, "Send thread was interrupted", ex);
        } finally {
            LOGGER.debug("{}joining connect notification thread", sendLogPrefix);
            try {
                connectNotificationThread.join();
            } catch (InterruptedException ex) {
                LOGGER.warn("{}interrupted while trying to join connect notification thread while stopping", sendLogPrefix);
            }

            LOGGER.debug("{}stopping", sendLogPrefix);
            shutdown();
        }
    }

    private void runProcessingThread() {
        String procLogPrefix = logPrefix + "[proc] ";
        LOGGER.debug("{}starting", procLogPrefix);

        long inboundQueueTimeoutMillis = INBOUND_QUEUE_TIMEOUT.toMillis();

        try {
            while (!shouldShutdown.get()) {
                List<ImmutablePair<Instant, String>> received = null;

                synchronized (inboundQueue) {
                    if (!inboundQueue.isEmpty()) {
                        received = new ArrayList<>(inboundQueue);
                        inboundQueue.clear();
                    }
                }

                if (received != null) {
                    for (ImmutablePair<Instant, String> pair : received) {
                        Instant timestamp = pair.getLeft();
                        String line = pair.getRight();

                        LOGGER.trace("{}received at {}: {}", procLogPrefix, timestamp, line);
                        ReceivedMessage msg = parseReceivedMessage(timestamp, line);
                        LOGGER.trace("{}received: {}", procLogPrefix, msg);

                        if (msg instanceof ServerMessage) {
                            if (msg.getType() == ReceivedMessage.Type.ERROR) {
                                LOGGER.warn("{}Received global error message: {}", logPrefix, msg);
                                throw new XPRCException(client, Consequence.RECONNECT, "Received global error message: " + msg);
                            } else {
                                LOGGER.info("{}Received unhandled global message: {}", logPrefix, msg);
                                continue;
                            }
                        }

                        if (!(msg instanceof ChannelMessage)) {
                            throw new IllegalArgumentException("Unhandled message class " + msg.getClass().getCanonicalName() + ": " + msg);
                        }

                        ChannelMessage channelMessage = (ChannelMessage) msg;
                        ChannelId channelId = channelMessage.getChannelId();
                        Channel<?, ?, ?> channel;
                        synchronized (channels) {
                            channel = channels.get(channelId);
                        }
                        if (channel == null) {
                            throw new IllegalArgumentException("Channel " + channelId + " is not active: " + channelMessage);
                        }

                        channel.process(channelMessage);

                        if (channel.isClosed()) {
                            LOGGER.debug("{}Closed: {}", procLogPrefix, channel);

                            synchronized (channels) {
                                channels.remove(channelId);
                            }

                            channelPool.releaseChannel(channelId);

                            notifyMonitorsAboutChannel(SessionMonitor::onChannelClosed, channel);
                        }

                        // FIXME: implement actual processing
                        // TODO: monitor processing backlog/delay (configurable limits for warning and reconnect)
                    }
                }

                synchronized (inboundQueue) {
                    if (inboundQueue.isEmpty() && !shouldShutdown.get()) {
                        LOGGER.debug("{}waiting for data to process", procLogPrefix); // DEBUG
                        inboundQueue.wait(inboundQueueTimeoutMillis);
                    }
                }
            }
        } catch (XPRCException ex) {
            ex.sendTo(client.getExceptionHandler());
        } catch (InterruptedException ex) {
            XPRCException.sendTo(client.getExceptionHandler(), client, Consequence.UNRECOVERABLE, "Processing thread was interrupted", ex);
        } catch (Exception ex) {
            XPRCException.sendTo(client.getExceptionHandler(), client, Consequence.RECONNECT, "Unhandled exception in processing thread", ex);
        }

        LOGGER.debug("{}stopping", procLogPrefix);
        shutdown();
    }

    private ReceivedMessage parseReceivedMessage(Instant timestamp, String line) {
        if (ReceivedMessage.isGloballyAddressed(line)) {
            return new ServerMessage(timestamp, line);
        } else {
            return new ChannelMessage(timestamp, line);
        }
    }

    public <CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> CH submitCommand(ChannelFactory<CH, C, M> channelFactory) {
        return submitCommand(channelFactory.getCommand(), channelFactory);
    }

    public <CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> CH submitCommand(Command.Builder<?, C, CH, ?, M> commandBuilder, ChannelFactory<CH, C, M> channelFactory) {
        return submitCommand(commandBuilder.build(), channelFactory);
    }

    public <CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> CH submitCommand(C command, ChannelFactory<CH, C, M> channelFactory) {
        ChannelId channelId = channelPool.allocateChannel()
                                         .orElseThrow(() -> new XPRCException(client, Consequence.RECONNECT, "channels are exhausted"));

        try {
            return submitCommand(channelId, command, channelFactory);
        } catch (Exception ex) {
            channelPool.dropChannel(channelId);
            throw new XPRCException(client, Consequence.RECONNECT, "Failed to submit command to automatically allocated channel " + channelId, ex);
        }
    }

    public <CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> CH submitCommand(ChannelId channelId, Command.Builder<?, C, CH, ?, M> commandBuilder, ChannelFactory<CH, C, M> channelFactory) {
        return submitCommand(channelId, commandBuilder.build(), channelFactory);
    }

    public <CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> CH submitCommand(ChannelId channelId, C command, ChannelFactory<CH, C, M> channelFactory) {
        if (shouldShutdown.get()) {
            LOGGER.warn("{}Session is being shut down, no more commands can be submitted. Got: {}", logPrefix, command);
            throw new XPRCException(client, Consequence.RECONNECT, "Session is being shut down");
        }

        LOGGER.debug("{}Queueing channel {}: {}", logPrefix, channelId, command);

        String encodedMessage;
        try {
            encodedMessage = command.encodeRequest(channelId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Command failed to encode: " + command, ex);
        }

        CH channel = channelFactory.createChannel(channelId, this, command);
        Channel<?, ?, ?> previousChannel;
        synchronized (channels) {
            previousChannel = channels.putIfAbsent(channelId, channel);
        }
        if (previousChannel != null) {
            // do not drop the channel from pool again - client should reconnect instead
            LOGGER.warn("{}Channel {} is already in use", logPrefix, channelId);
            throw new XPRCException(
                client,
                Consequence.RECONNECT,
                "Channel " + channelId + " is already in use: " + previousChannel
            );
        }

        synchronized (outboundQueue) {
            outboundQueue.addLast(ImmutablePair.of(channel, encodedMessage));
            outboundQueue.notifyAll();
        }

        return channel;
    }

    public boolean sendRawMessage(String msg) {
        if (shouldShutdown.get()) {
            return false;
        }

        synchronized (outboundQueue) {
            outboundQueue.addLast(ImmutablePair.of(null, msg));
            outboundQueue.notifyAll();
        }

        return true;
    }

    @Override
    public String toString() {
        return "Session(" + localReferenceTimestamp + ", " + client + ")";
    }
}
