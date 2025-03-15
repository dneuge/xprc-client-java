package de.energiequant.xprc;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.energiequant.xprc.utils.WaitUtils;

public abstract class Channel<SELF extends Channel<SELF, C, M>, C extends Command<?, SELF, C, M>, M extends ChannelMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Channel.class);

    private final String channelLogPrefix;
    private final ChannelId id;
    private final C command;
    private final Session session;

    private Instant dispatched;
    private Instant confirmed;
    private Instant closed;
    private volatile State state = State.PREPARED; // FIXME: should probably be an AtomicReference instead

    private final AtomicBoolean requestedTermination = new AtomicBoolean(false);

    private final Callbacks<SELF, C, M> externalCallbacks;

    private static final Duration WAIT_CHECK_INTERVAL = Duration.ofMillis(50);

    public enum State {
        PREPARED, DISPATCHED, ACKNOWLEDGED, FINISHED, ERROR;

        private boolean awaits(ReceivedMessage.Type messageType) {
            switch (messageType) {
                case ACKNOWLEDGEMENT:
                    return this == DISPATCHED;

                case ERROR:
                case FINALIZATION:
                    return impliesOpenChannel();

                case CONTINUATION:
                    return this == ACKNOWLEDGED;

                default:
                    throw new IllegalArgumentException("Unhandled message type: " + messageType);
            }
        }

        private State transition(ReceivedMessage.Type messageType) {
            if (!awaits(messageType)) {
                throw new IllegalArgumentException("Received message type " + messageType + " is invalid for command state " + this);
            }

            switch (messageType) {
                case ACKNOWLEDGEMENT:
                case CONTINUATION:
                    return ACKNOWLEDGED;

                case FINALIZATION:
                    return FINISHED;

                case ERROR:
                    return ERROR;

                default:
                    throw new IllegalArgumentException("Unhandled message type: " + messageType);
            }
        }

        private boolean awaitsDispatch() {
            return this == PREPARED;
        }

        private boolean impliesOpenChannel() {
            return this == DISPATCHED || this == ACKNOWLEDGED;
        }

        private boolean impliesClosedChannel() {
            return this == ERROR || this == FINISHED;
        }

        private boolean hasBeenConfirmed() {
            return this == ACKNOWLEDGED || this == FINISHED || this == ERROR;
        }
    }

    @FunctionalInterface
    public interface StateChangeCallback<CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> {
        // FIXME: message may not be present, encapsulate reason instead
        void accept(CH channel, M msg, Channel.State oldState, Channel.State newState);
    }

    public static class Callbacks<CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> {
        private final StateChangeCallback<CH, C, M> onStateChanging;
        private final StateChangeCallback<CH, C, M> onStateChanged;
        private final BiConsumer<CH, M> onDataMessage;
        private final BiConsumer<CH, M> onErrorMessage;
        private final BiConsumer<CH, M> onBlankMessage;
        private final Consumer<CH> onDispatch;
        private final Consumer<CH> onConfirmation;
        private final Consumer<CH> onTermination;

        private Callbacks(StateChangeCallback<CH, C, M> onStateChanging, StateChangeCallback<CH, C, M> onStateChanged, BiConsumer<CH, M> onDataMessage, BiConsumer<CH, M> onErrorMessage, BiConsumer<CH, M> onBlankMessage, Consumer<CH> onDispatch, Consumer<CH> onConfirmation, Consumer<CH> onTermination) {
            this.onStateChanging = onStateChanging;
            this.onStateChanged = onStateChanged;
            this.onDataMessage = onDataMessage;
            this.onErrorMessage = onErrorMessage;
            this.onBlankMessage = onBlankMessage;
            this.onDispatch = onDispatch;
            this.onConfirmation = onConfirmation;
            this.onTermination = onTermination;
        }

        public static <CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> Builder<CH, C, M> builder() {
            return new Builder<>();
        }

        public static class Builder<CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> {
            private StateChangeCallback<CH, C, M> onStateChanging;
            private StateChangeCallback<CH, C, M> onStateChanged;
            private BiConsumer<CH, M> onDataMessage;
            private BiConsumer<CH, M> onErrorMessage;
            private BiConsumer<CH, M> onBlankMessage;
            private Consumer<CH> onDispatch;
            private Consumer<CH> onConfirmation;
            private Consumer<CH> onTermination;

            public Builder<CH, C, M> onStateChanging(StateChangeCallback<CH, C, M> onStateChanging) {
                this.onStateChanging = onStateChanging;
                return this;
            }

            public Builder<CH, C, M> onStateChanged(StateChangeCallback<CH, C, M> onStateChanged) {
                this.onStateChanged = onStateChanged;
                return this;
            }

            public Builder<CH, C, M> onDataMessage(BiConsumer<CH, M> onDataMessage) {
                this.onDataMessage = onDataMessage;
                return this;
            }

            public Builder<CH, C, M> onErrorMessage(BiConsumer<CH, M> onErrorMessage) {
                this.onErrorMessage = onErrorMessage;
                return this;
            }

            public Builder<CH, C, M> onBlankMessage(BiConsumer<CH, M> onBlankMessage) {
                this.onBlankMessage = onBlankMessage;
                return this;
            }

            public Builder<CH, C, M> onDispatch(Consumer<CH> onDispatch) {
                this.onDispatch = onDispatch;
                return this;
            }

            public Builder<CH, C, M> onConfirmation(Consumer<CH> onConfirmation) {
                this.onConfirmation = onConfirmation;
                return this;
            }

            public Builder<CH, C, M> onTermination(Consumer<CH> onTermination) {
                this.onTermination = onTermination;
                return this;
            }

            public Callbacks<CH, C, M> build() {
                return new Callbacks<>(
                    onStateChanging, onStateChanged,
                    onDataMessage, onErrorMessage, onBlankMessage,
                    onDispatch, onConfirmation, onTermination
                );
            }
        }
    }

    public Channel(ChannelId id, Session session, C command) {
        this(id, session, command, Callbacks.<SELF, C, M>builder().build());
    }

    public Channel(ChannelId id, Session session, C command, Callbacks<SELF, C, M> externalCallbacks) {
        this.id = id;
        this.command = command;
        this.session = session;
        this.externalCallbacks = externalCallbacks;

        this.channelLogPrefix = session.getLogPrefix() + "[" + id + "] ";
    }

    public C getCommand() {
        return command;
    }

    public Session getSession() {
        return session;
    }

    void onDispatch() {
        // message must not be sent to server before this method has been called

        synchronized (this) {
            if (dispatched != null) {
                throw new IllegalArgumentException("Command has already been dispatched at " + dispatched);
            }

            if (!state.awaitsDispatch()) {
                throw new IllegalArgumentException("Dispatch is not expected (command state: " + state + ")");
            }

            dispatched = Instant.now();
            state = State.DISPATCHED;
        }
    }

    void process(ChannelMessage channelMessage) {
        synchronized (this) {
            if (dispatched == null) {
                throw new IllegalArgumentException("Command has not been dispatched yet; received: " + channelMessage);
            }

            if (closed != null) {
                throw new IllegalArgumentException("Command is closed; received: " + channelMessage);
            }

            if (state.impliesClosedChannel()) {
                throw new IllegalArgumentException(
                    "Command is in final state " + state
                        + ", no more messages are expected; received: " + channelMessage
                );
            }

            M msg = decode(channelMessage);
            LOGGER.debug("{}received: {}", channelLogPrefix, msg);

            ReceivedMessage.Type messageType = msg.getType();

            // state transition includes validation; we should validate before we continue
            State newState = state.transition(messageType);
            if (newState != state) {
                LOGGER.debug("{}command state {} => {}", channelLogPrefix, state, newState);
                handleStateChanging(state, newState, msg);
            }

            if (messageType.opensChannel()) {
                if (confirmed != null) {
                    throw new IllegalArgumentException("Command has already been confirmed; received: " + msg);
                }

                confirmed = msg.getLocalReceiveTimestamp();
                handleConfirmation(msg);
            } else if (messageType.closesChannel() && confirmed == null) {
                confirmed = msg.getLocalReceiveTimestamp();
                handleConfirmation(msg);
            }

            if (messageType == ReceivedMessage.Type.ERROR) {
                handleErrorMessage(msg);
            } else if (msg.containsData()) {
                handleDataMessage(msg);
            } else {
                handleBlankMessage(msg);
            }

            if (messageType.closesChannel()) {
                closed = msg.getLocalReceiveTimestamp();
                handleTermination(msg);
            }

            if (newState != state) {
                State oldState = state;
                state = newState;
                handleStateChanged(oldState, newState, msg);
            }
        }
    }

    /**
     * Decodes the given {@link ChannelMessage} to a command-specific data structure.
     * <p>
     * While decoding may use current channel context (based on previously processed events/messages)
     * <strong>states must not be changed yet</strong>. The decoded message will be passed back into event callbacks
     * for processing at the appropriate time.
     * </p>
     *
     * @param msg generically decoded message
     * @return command-specific decoded message
     */
    protected abstract M decode(ChannelMessage msg);

    @SuppressWarnings("unchecked")
    private void handleStateChanging(State oldState, State newState, M msg) {
        onStateChanging(oldState, newState, msg);

        if (externalCallbacks.onStateChanging != null) {
            try {
                externalCallbacks.onStateChanging.accept((SELF) this, msg, oldState, newState);
            } catch (Exception ex) {
                XPRCClient client = session.getClient();
                XPRCException.sendTo(client.getExceptionHandler(), client, XPRCException.Consequence.DOWNSTREAM, "custom channel callback for onStateChanging failed", ex);
            }
        }
    }

    /**
     * Called before other event hooks to announce how a state will be changing.
     * See also {@link #onStateChanged(State, State, ChannelMessage)} which will be called after all other hooks when
     * the {@link Channel}'s state has actually been updated.
     *
     * @param oldState current state
     * @param newState state that will be transitioned to
     * @param msg      message triggering the transition; <code>null</code> if unavailable
     * @see #onStateChanged(State, State, ChannelMessage)
     */
    protected void onStateChanging(State oldState, State newState, M msg) {
        // extension point for implementing classes
    }

    @SuppressWarnings("unchecked")
    private void handleConfirmation(M msg) {
        onConfirmation(msg);

        if (externalCallbacks.onConfirmation != null) {
            try {
                externalCallbacks.onConfirmation.accept((SELF) this);
            } catch (Exception ex) {
                XPRCClient client = session.getClient();
                XPRCException.sendTo(client.getExceptionHandler(), client, XPRCException.Consequence.DOWNSTREAM, "custom channel callback for onConfirmation failed", ex);
            }
        }
    }

    /**
     * Called when the {@link Channel} is being confirmed, i.e. the first message has been received. Note that
     * confirmation does not indicate any status other than the server having processed the initial request.
     * The message could indicate an error or not hold any payload.
     * <p>
     * This method is being called before the channel state will be updated.
     * </p>
     *
     * @param msg message triggering confirmation
     */
    protected void onConfirmation(M msg) {
        // extension point for implementing classes
    }

    @SuppressWarnings("unchecked")
    private void handleTermination(M msg) {
        onTermination(msg);

        if (externalCallbacks.onTermination != null) {
            try {
                externalCallbacks.onTermination.accept((SELF) this);
            } catch (Exception ex) {
                XPRCClient client = session.getClient();
                XPRCException.sendTo(client.getExceptionHandler(), client, XPRCException.Consequence.DOWNSTREAM, "custom channel callback for onTermination failed", ex);
            }
        }
    }

    /**
     * Called when the {@link Channel} has been terminated. While this usually happens when a {@link ChannelMessage}
     * indicates server-side channel termination (error or command completion), it should also be expected that the
     * channel terminates without a message when a {@link Session} is being closed (by either server or client).
     * <p>
     * This method is being called before the channel state will be updated.
     * </p>
     *
     * @param msg message triggering termination; <code>null</code> if unavailable
     */
    protected void onTermination(M msg) {
        // extension point for implementing classes
    }

    @SuppressWarnings("unchecked")
    private void handleStateChanged(State oldState, State newState, M msg) {
        onStateChanged(oldState, newState, msg);

        if (externalCallbacks.onStateChanged != null) {
            try {
                externalCallbacks.onStateChanged.accept((SELF) this, msg, oldState, newState);
            } catch (Exception ex) {
                XPRCClient client = session.getClient();
                XPRCException.sendTo(client.getExceptionHandler(), client, XPRCException.Consequence.DOWNSTREAM, "custom channel callback for onStateChanged failed", ex);
            }
        }
    }

    /**
     * Called after all other event hooks to announce what state the {@link Channel} has transitioned to.
     * See also {@link #onStateChanging(State, State, ChannelMessage)} for early notification of an upcoming transition.
     *
     * @param oldState previous state
     * @param newState state that has just been transitioned to
     * @param msg      message triggering the transition; <code>null</code> if unavailable
     * @see #onStateChanging(State, State, ChannelMessage)
     */
    protected void onStateChanged(State oldState, State newState, M msg) {
        // extension point for implementing classes
    }

    @SuppressWarnings("unchecked")
    private void handleErrorMessage(M msg) {
        onErrorMessage(msg);

        if (externalCallbacks.onErrorMessage != null) {
            try {
                externalCallbacks.onErrorMessage.accept((SELF) this, msg);
            } catch (Exception ex) {
                XPRCClient client = session.getClient();
                XPRCException.sendTo(client.getExceptionHandler(), client, XPRCException.Consequence.DOWNSTREAM, "custom channel callback for onErrorMessage failed", ex);
            }
        }
    }

    /**
     * Called when the {@link Channel} is being terminated by an error message.
     * The payload of an error message is usually a human-readable and implementation-specific explanation of what went
     * wrong, if present. Error messages are not standardized.
     * <p>
     * This method is being called before the channel state will be updated.
     * </p>
     *
     * @param msg message indicating an error
     */
    protected void onErrorMessage(M msg) {
        // extension point for implementing classes
    }

    @SuppressWarnings("unchecked")
    private void handleDataMessage(M msg) {
        // TODO: some channels call listeners onDataMessage which may not make sense before the general callbacks are processed - do we need an extra phase?
        onDataMessage(msg);

        if (externalCallbacks.onDataMessage != null) {
            try {
                externalCallbacks.onDataMessage.accept((SELF) this, msg);
            } catch (Exception ex) {
                XPRCClient client = session.getClient();
                XPRCException.sendTo(client.getExceptionHandler(), client, XPRCException.Consequence.DOWNSTREAM, "custom channel callback for onDataMessage failed", ex);
            }
        }
    }

    /**
     * Called when the {@link Channel} received a (non-error) message containing (command-specific) data.
     * <p>
     * This method is being called before the channel state will be updated.
     * </p>
     *
     * @param msg message holding (command-specific) data
     */
    protected void onDataMessage(M msg) {
        // extension point for implementing classes
    }

    @SuppressWarnings("unchecked")
    private void handleBlankMessage(M msg) {
        onBlankMessage(msg);

        if (externalCallbacks.onBlankMessage != null) {
            try {
                externalCallbacks.onBlankMessage.accept((SELF) this, msg);
            } catch (Exception ex) {
                XPRCClient client = session.getClient();
                XPRCException.sendTo(client.getExceptionHandler(), client, XPRCException.Consequence.DOWNSTREAM, "custom channel callback for onBlankMessage failed", ex);
            }
        }
    }

    /**
     * Called when the {@link Channel} received a (non-error) message without (command-specific) data.
     * <p>
     * This method is being called before the channel state will be updated.
     * </p>
     *
     * @param msg message holding no (command-specific) data
     */
    protected void onBlankMessage(M msg) {
        // extension point for implementing classes
    }

    public Optional<State> getCommandState() {
        synchronized (this) {
            return Optional.ofNullable(state);
        }
    }

    public boolean isClosed() {
        synchronized (this) {
            return (closed != null);
        }
    }

    public void close() {
        // FIXME: implement - send termination request via Session if not already closed
    }

    /**
     * Blocks until the channel has been confirmed or the timeout is reached.
     * <p>
     * Call {@link #getCommandState()} to check if the channel has actually been confirmed.
     * </p>
     *
     * @param timeout maximum time to wait for channel confirmation
     * @return same channel instance for method-chaining
     * @see #getCommandState()
     */
    public SELF waitUntilConfirmed(Duration timeout) {
        // FIXME: use object monitor instead (synchronize over state to remove "volatile" flag as well)
        WaitUtils.sleepFor(timeout, WAIT_CHECK_INTERVAL, () -> state.hasBeenConfirmed(), channelLogPrefix);
        return (SELF) this;
    }

    /**
     * Blocks until the channel has been terminated or the timeout is reached.
     * Channels can be terminated regularly at the successful completion of a command, when an error occurs server-side
     * or when the session has been closed.
     * <p>
     * Call {@link #getCommandState()} to check if the channel has actually been terminated and whether the command has
     * successfully {@link State#FINISHED} or an {@link State#ERROR} has occurred.
     * </p>
     *
     * @param timeout maximum time to wait for channel termination
     * @return same channel instance for method-chaining
     * @see #getCommandState()
     */
    public SELF waitUntilTerminated(Duration timeout) {
        // FIXME: use object monitor instead (synchronize over state to remove "volatile" flag as well)
        WaitUtils.sleepFor(timeout, WAIT_CHECK_INTERVAL, () -> state.impliesClosedChannel(), channelLogPrefix);
        return (SELF) this;
    }

    /**
     * Asynchronously requests the channel to be terminated unless it already is.
     * Combine with {@link #waitUntilTerminated(Duration)} in case actual termination should be waited for.
     */
    public void terminateAsync() {
        if (!state.impliesClosedChannel()) {
            boolean alreadyRequested = requestedTermination.getAndSet(true);
            if (!alreadyRequested) {
                session.sendRawMessage(id + " TERM");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Channel(");

        sb.append(id);
        sb.append("/");
        sb.append(state);
        sb.append(", ");
        sb.append(command);

        if (dispatched != null) {
            sb.append(", dispatched=");
            sb.append(dispatched);
        }

        if (confirmed != null) {
            sb.append(", confirmed=");
            sb.append(confirmed);
        }

        if (closed != null) {
            sb.append(", closed=");
            sb.append(closed);
        }

        sb.append(", ");
        sb.append(session);

        sb.append(")");

        return sb.toString();
    }
}
