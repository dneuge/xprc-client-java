package de.energiequant.xprc;

public interface SessionMonitor {
    // FIXME: connect

    enum Direction {
        CLIENT_TO_SERVER, SERVER_TO_CLIENT;
    }

    void onConnected(Session session);

    void onDispatched(Channel channel);

    void onRawMessage(Session session, Direction direction, String message);

    void onTimeout(Channel channel);

    void onChannelClosed(Channel channel);

    void onSessionClosed(Session session);

    class SessionMonitorAdapter implements SessionMonitor {
        @Override
        public void onConnected(Session session) {
            // do nothing by default, override as needed
        }

        @Override
        public void onDispatched(Channel channel) {
            // do nothing by default, override as needed
        }

        @Override
        public void onRawMessage(Session session, Direction direction, String message) {
            // do nothing by default, override as needed
        }

        @Override
        public void onTimeout(Channel channel) {
            // do nothing by default, override as needed
        }

        @Override
        public void onChannelClosed(Channel channel) {
            // do nothing by default, override as needed
        }

        @Override
        public void onSessionClosed(Session session) {
            // do nothing by default, override as needed
        }
    }
}