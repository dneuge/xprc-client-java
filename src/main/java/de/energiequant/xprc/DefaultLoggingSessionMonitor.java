package de.energiequant.xprc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLoggingSessionMonitor implements SessionMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLoggingSessionMonitor.class);

    @Override
    public void onConnected(Session session) {
        LOGGER.debug("connected {}", session);
    }

    @Override
    public void onDispatched(Channel channel) {
        LOGGER.debug("dispatched {}", channel);
    }

    @Override
    public void onRawMessage(Session session, Direction direction, String message) {
        LOGGER.debug("raw message on {}: {} \"{}\"", session, direction, message);
    }

    @Override
    public void onTimeout(Channel channel) {
        LOGGER.debug("timeout: {}", channel);
    }

    @Override
    public void onChannelClosed(Channel channel) {
        LOGGER.debug("closed {}", channel);
    }

    @Override
    public void onSessionClosed(Session session) {
        LOGGER.debug("closed {}", session);
    }
}
