package de.energiequant.xprc.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.energiequant.xprc.XPRCClient;

/**
 * Collects all entries from an SRID ("Server Identify") command into a {@link SRIDResult}.
 *
 * <p>
 * The aggregator needs to hook into data, error and termination callbacks and can be started either directly via any
 * {@code submitCommand} method provided by this class, {@link SRIDCommandBuilder#submitAndAggregate()} or
 * {@link SRIDChannel.FactoryBuilder#submitAndAggregate()}.
 * </p>
 */
public class SRIDAggregator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SRIDAggregator.class);

    private final CompletableFuture<SRIDResult> future = new CompletableFuture<>();

    private final Map<String, String> collected = new HashMap<>();
    private String error = null;

    private SRIDAggregator() {
        // use submitCommand; hide constructor
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static CompletableFuture<SRIDResult> submitCommand(SRIDChannel.FactoryBuilder cfb) {
        SRIDAggregator aggregator = new SRIDAggregator();

        cfb.onDataMessage((ch, msg) -> aggregator.onDataMessage((SRIDChannel) ch, (SRIDMessage) msg))
           .onErrorMessage((ch, msg) -> aggregator.onDataMessage((SRIDChannel) ch, (SRIDMessage) msg))
           .onTermination(ch -> aggregator.onTermination((SRIDChannel) ch))
           .submit();

        return aggregator.future;
    }

    @SuppressWarnings({"rawtypes"})
    public static CompletableFuture<SRIDResult> submitCommand(SRIDCommandBuilder cb) {
        return submitCommand((SRIDChannel.FactoryBuilder) cb.prepareChannel());
    }

    @SuppressWarnings({"rawtypes"})
    public static CompletableFuture<SRIDResult> submitCommand(XPRCClient client) {
        return submitCommand((SRIDCommandBuilder) client.commandBuilders().srid());
    }

    private void error(String error) {
        LOGGER.warn("Error encountered while aggregating SRID response: {}", error);

        // only record first error
        if (this.error == null) {
            this.error = error;
        }
    }

    private void error(String error, Function<String, RuntimeException> exceptionConstructor) {
        error(error);
        throw exceptionConstructor.apply(error);
    }

    @SuppressWarnings("rawtypes")
    private void onDataMessage(SRIDChannel ch, SRIDMessage msg) {
        String key = msg.getKey().orElse(null);
        String value = msg.getValue().orElse(null);

        if (key == null || value == null) {
            error("received SRID data message which could not be parsed to a key/value pair (invalid per spec): " + msg, IllegalArgumentException::new);
        }

        String previous = collected.put(key, value);
        if (previous != null) {
            error("duplicate key \"" + key + "\", received \"" + previous + "\" and \"" + value + "\"", IllegalArgumentException::new);
        }
    }

    @SuppressWarnings("rawtypes")
    private void onErrorMessage(SRIDChannel ch, SRIDMessage msg) {
        String raw = msg.getRaw();

        String out;
        if (raw.trim().isEmpty()) {
            out = "server error";
        } else {
            out = "server error: " + raw;
        }

        error(out);
    }

    @SuppressWarnings("rawtypes")
    private void onTermination(SRIDChannel ch) {
        if (error != null) {
            future.completeExceptionally(new ExecutionFailed(error));
            return;
        }

        future.complete(new SRIDResult(this));
    }

    public static class SRIDResult {
        private final Map<String, String> collected;

        SRIDResult(SRIDAggregator aggregator) {
            this.collected = Collections.unmodifiableMap(aggregator.collected);
        }

        public Optional<String> get(String key) {
            return Optional.ofNullable(collected.get(key));
        }

        public Optional<String> get(SRIDMessage.StandardKey key) {
            return get(key.getEncoding());
        }

        public Stream<Map.Entry<String, String>> streamAll() {
            return getAll().entrySet().stream();
        }

        public Map<String, String> getAll() {
            return new HashMap<>(collected);
        }
    }

    private static class ExecutionFailed extends RuntimeException {
        ExecutionFailed(String msg) {
            super(msg);
        }
    }
}
