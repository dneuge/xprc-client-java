package de.energiequant.xprc.commands;

import java.util.Collection;
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
import de.energiequant.xprc.commands.SRLCMessage.SRLCDescriptor;

/**
 * Collects all entries from an SRLC ("List Server Commands") command into a {@link SRLCResult}.
 *
 * <p>
 * The aggregator needs to hook into data, error and termination callbacks and can be started either directly via any
 * {@code submitCommand} method provided by this class, {@link SRLCCommandBuilder#submitAndAggregate()} or
 * {@link SRLCChannel.FactoryBuilder#submitAndAggregate()}.
 * </p>
 */
public class SRLCAggregator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SRLCAggregator.class);

    private final CompletableFuture<SRLCResult> future = new CompletableFuture<>();

    private final Map<String, SRLCDescriptor> collected = new HashMap<>();
    private String error = null;

    private SRLCAggregator() {
        // use submitCommand; hide constructor
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static CompletableFuture<SRLCResult> submitCommand(SRLCChannel.FactoryBuilder cfb) {
        SRLCAggregator aggregator = new SRLCAggregator();

        cfb.onDataMessage((ch, msg) -> aggregator.onDataMessage((SRLCChannel) ch, (SRLCMessage) msg))
           .onErrorMessage((ch, msg) -> aggregator.onDataMessage((SRLCChannel) ch, (SRLCMessage) msg))
           .onTermination(ch -> aggregator.onTermination((SRLCChannel) ch))
           .submit();

        return aggregator.future;
    }

    @SuppressWarnings({"rawtypes"})
    public static CompletableFuture<SRLCResult> submitCommand(SRLCCommandBuilder cb) {
        return submitCommand((SRLCChannel.FactoryBuilder) cb.prepareChannel());
    }

    @SuppressWarnings({"rawtypes"})
    public static CompletableFuture<SRLCResult> submitCommand(XPRCClient client) {
        return submitCommand((SRLCCommandBuilder) client.commandBuilders().srlc());
    }

    private void error(String error) {
        LOGGER.warn("Error encountered while aggregating SRLC response: {}", error);

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
    private void onDataMessage(SRLCChannel ch, SRLCMessage msg) {
        SRLCDescriptor descriptor = msg.getDescriptor().orElse(null);
        if (descriptor == null) {
            error("received SRLC data message which could not be parsed (invalid per spec): " + msg, IllegalArgumentException::new);
        }

        SRLCDescriptor previous = collected.put(descriptor.getCommandName(), descriptor);
        if (previous != null) {
            error("duplicate command descriptor received for \"" + descriptor.getCommandName() + "\"", IllegalArgumentException::new);
        }
    }

    @SuppressWarnings("rawtypes")
    private void onErrorMessage(SRLCChannel ch, SRLCMessage msg) {
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
    private void onTermination(SRLCChannel ch) {
        if (error != null) {
            future.completeExceptionally(new ExecutionFailed(error));
            return;
        }

        future.complete(new SRLCResult(this));
    }

    public static class SRLCResult {
        private final Map<String, SRLCDescriptor> collected;

        SRLCResult(SRLCAggregator aggregator) {
            this.collected = Collections.unmodifiableMap(aggregator.collected);
        }

        /**
         * Indicates if the descriptors of this result have been reliably parsed.
         *
         * <p>
         * This is equivalent to checking for SRLC's own descriptor for the version we expected while parsing all
         * messages. If the version does not match, the entire result may be invalid. For mitigation, the SRLC command
         * should be attempted to be switched to a compatible version using SRFS as described for the recommended
         * session initiation flow in the protocol specification.
         * </p>
         *
         * @return {@code true} if all descriptors were parsed as expected; {@code false} may indicate an unsupported SRLC command version being active
         */
        public boolean isReliable() {
            SRLCDescriptor descriptor = describe("SRLC").orElse(null);
            if (descriptor == null) {
                return false;
            }

            return SRLCMessage.isExpectedCommandVersion(descriptor.getActiveVersion());
        }

        public Optional<SRLCDescriptor> describe(String commandName) {
            return Optional.ofNullable(collected.get(commandName));
        }

        public Stream<SRLCDescriptor> streamAllDescriptors() {
            return getAllDescriptors().stream();
        }

        public Collection<SRLCDescriptor> getAllDescriptors() {
            return collected.values();
        }

        public Map<String, SRLCDescriptor> getIndexedDescriptors() {
            return new HashMap<>(collected);
        }
    }

    private static class ExecutionFailed extends RuntimeException {
        ExecutionFailed(String msg) {
            super(msg);
        }
    }
}
