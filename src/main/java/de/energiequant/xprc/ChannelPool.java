package de.energiequant.xprc;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allocates channel IDs used within a {@link Session}.
 *
 * <p>
 * Each ID needs to be unique per session until the associated channel has been terminated by both parties
 * (client and server).
 * As this may only complete with a delay unknown to the client, channels released back to the pool can only be
 * reallocated after the {@link #blockDuration} has passed.
 * See {@link XPRCClientFactory#setChannelBlockDuration(Duration)} for additional details.
 * </p>
 *
 * <p>
 * Allocation requests can be made either for a specific channel ID or anonymously which will return any available ID.
 * </p>
 */
public class ChannelPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);

    private final String logPrefix;
    private final Map<Integer, State> stateByChannel = new TreeMap<>();
    private final Duration blockDuration;
    private final AtomicInteger nextChannelNumber = new AtomicInteger(ChannelId.FIRST_NUMERIC);

    private static final State ALLOCATED = new State(true, Instant.MAX);

    private static class State {
        private final boolean isAllocated;
        private final Instant blockedUntil;

        private State(boolean isAllocated, Instant blockedUntil) {
            this.isAllocated = isAllocated;
            this.blockedUntil = blockedUntil;
        }

        private boolean blockExpired() {
            return Instant.now().isAfter(blockedUntil);
        }

        @Override
        public String toString() {
            return "State(allocated=" + isAllocated + ", blockedUntil=" + blockedUntil + ")";
        }

        private static boolean isFree(State state) {
            return state == null || !state.isAllocated || state.blockExpired();
        }
    }

    public ChannelPool(String logPrefix, Duration blockDuration) {
        this.logPrefix = logPrefix;
        this.blockDuration = blockDuration;
    }

    public boolean isChannelBlocked(ChannelId channelId) {
        synchronized (this) {
            return !State.isFree(stateByChannel.get(channelId.getNumeric()));
        }
    }

    public boolean isChannelBlocked(String channelId) {
        return isChannelBlocked(ChannelId.fromAlphanumeric(channelId));
    }

    public Optional<ChannelId> allocateChannel() {
        synchronized (this) {
            for (int i = 0; i < ChannelId.NUM_POSSIBLE_CHANNELS; i++) {
                int channelNumber = nextChannelNumber.getAndUpdate(ChannelId::incrementNumeric);
                if (State.isFree(stateByChannel.get(channelNumber))) {
                    return allocateChannel(ChannelId.fromNumeric(channelNumber));
                }
            }
        }

        LOGGER.warn("{}channel pool is exhausted", logPrefix);
        return Optional.empty();
    }

    public Optional<ChannelId> allocateChannel(ChannelId channelId) {
        synchronized (this) {
            int key = channelId.getNumeric();
            State state = stateByChannel.get(key);
            if (state != null) {
                if (state.isAllocated) {
                    LOGGER.debug("{}requested channel {} is already allocated", logPrefix, channelId);
                    return Optional.empty();
                }

                if (!state.blockExpired()) {
                    LOGGER.debug("{}requested channel {} is still blocked until {}", logPrefix, channelId, state.blockedUntil);
                    return Optional.empty();
                }
            }

            stateByChannel.put(key, ALLOCATED);
        }

        LOGGER.debug("{}allocated channel {}", logPrefix, channelId);
        return Optional.of(channelId);
    }

    public Optional<ChannelId> allocateChannel(String channelId) {
        return allocateChannel(ChannelId.fromAlphanumeric(channelId));
    }

    public void releaseChannel(ChannelId channelId) {
        State releaseState = new State(false, calculateEndOfBlock());
        synchronized (this) {
            stateByChannel.put(channelId.getNumeric(), releaseState);
        }

        LOGGER.debug("{}released channel {}: {}", logPrefix, channelId, releaseState);
    }

    private Instant calculateEndOfBlock() {
        return Instant.now().plus(blockDuration);
    }
}
