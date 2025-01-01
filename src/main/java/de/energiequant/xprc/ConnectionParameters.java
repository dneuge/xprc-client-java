package de.energiequant.xprc;

import java.time.Duration;
import java.util.function.Supplier;

public class ConnectionParameters {
    private final String alias;
    private final String host;
    private final int port;
    private final Supplier<char[]> passwordProvider;

    private final Duration connectTimeout;
    private final Duration reconnectDelay;

    private final Duration acknowledgeTimeout; // FIXME: not used

    private ConnectionParameters(String alias, String host, int port, Supplier<char[]> passwordProvider, Duration connectTimeout, Duration reconnectDelay, Duration acknowledgeTimeout) {
        this.alias = alias;
        this.host = host;
        this.port = port;
        this.passwordProvider = passwordProvider;
        this.connectTimeout = connectTimeout;
        this.reconnectDelay = reconnectDelay;
        this.acknowledgeTimeout = acknowledgeTimeout;
    }

    public String getAlias() {
        return alias;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Supplier<char[]> getPasswordProvider() {
        return passwordProvider;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getReconnectDelay() {
        return reconnectDelay;
    }

    public Duration getAcknowledgeTimeout() {
        return acknowledgeTimeout;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String alias = null;
        private String host = "localhost";
        private int port = -1;
        private Supplier<char[]> passwordProvider;

        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration reconnectDelay = Duration.ofSeconds(10);
        private Duration acknowledgeTimeout = Duration.ofSeconds(10);

        public Builder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setPasswordProvider(Supplier<char[]> passwordProvider) {
            this.passwordProvider = passwordProvider;
            return this;
        }

        public Builder setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setReconnectDelay(Duration reconnectDelay) {
            this.reconnectDelay = reconnectDelay;
            return this;
        }

        public Builder setAcknowledgeTimeout(Duration acknowledgeTimeout) {
            this.acknowledgeTimeout = acknowledgeTimeout;
            return this;
        }

        public ConnectionParameters build() {
            if (alias == null) {
                alias = host + ":" + port;
            }

            return new ConnectionParameters(alias, host, port, passwordProvider, connectTimeout, reconnectDelay, acknowledgeTimeout);
        }
    }

    public static class DefaultPasswordProviders {
        private DefaultPasswordProviders() {
            // utility class; hide constructor
        }

        public static Supplier<char[]> constant(String password) {
            return constant(password.toCharArray());
        }

        public static Supplier<char[]> constant(char[] password) {
            // client implementation will clear the array when done, so we need a copy
            return password::clone;
        }

        public static Supplier<char[]> readingXPRCPasswordFile(XPlaneInstance xplaneInstance) {
            return () -> xplaneInstance.readXPRCPassword()
                                       .orElseThrow(
                                           () -> new PasswordUnavailable(
                                               "Failed to read XPRC password from " + xplaneInstance
                                           )
                                       );
        }
    }

    private static class PasswordUnavailable extends RuntimeException {
        private PasswordUnavailable(String msg) {
            super(msg);
        }
    }
}
