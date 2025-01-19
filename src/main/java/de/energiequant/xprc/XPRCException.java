package de.energiequant.xprc;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPRCException extends RuntimeException {
    private static final Logger LOGGER = LoggerFactory.getLogger(XPRCException.class);

    public enum Consequence {
        UNRECOVERABLE,
        RESOURCE_LEAK,
        RECONNECT,
        /**
         * The error occurred within a callback given to the XPRC client by the application integrating it. The client
         * continues regardless; the error should be handled by the application, if relevant.
         */
        DOWNSTREAM;
    }

    private final transient XPRCClient client;
    private final String connectionAlias;
    private final Consequence consequence;

    public XPRCException(XPRCClient client, Consequence consequence, String msg) {
        super("[" + client.getConnectionParameters().getAlias() + "] " + consequence + ": " + msg);
        this.client = client;
        this.connectionAlias = client.getConnectionParameters().getAlias();
        this.consequence = consequence;
    }

    public XPRCException(XPRCClient client, Consequence consequence, String msg, Throwable cause) {
        super("[" + client.getConnectionParameters().getAlias() + "] " + consequence + ": " + msg, cause);
        this.client = client;
        this.connectionAlias = client.getConnectionParameters().getAlias();
        this.consequence = consequence;
    }

    public Optional<XPRCClient> getClient() {
        return Optional.ofNullable(client);
    }

    public String getConnectionAlias() {
        return connectionAlias;
    }

    public Consequence getConsequence() {
        return consequence;
    }

    public static void sendTo(Consumer<XPRCException> handler, XPRCClient client, Consequence consequence, String msg, Throwable cause) {
        XPRCException ex = new XPRCException(client, consequence, msg, cause);
        ex.fillInStackTrace();
        ex.sendTo(handler);
    }

    public void sendTo(Consumer<XPRCException> handler) {
        try {
            handler.accept(this);
        } catch (Exception ex) {
            LOGGER.warn("Forwarded exception handling failed", ex);
        }
    }
}
