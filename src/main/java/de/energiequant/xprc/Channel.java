package de.energiequant.xprc;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Channel<C extends Command<M>, M extends ChannelMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Channel.class);

    private final String sessionLogPrefix;
    private final ChannelId id;
    private final C command;
    private final Session session;
    private final ChannelDecoder<M> decoder;

    private Instant dispatched;
    private Instant confirmed;
    private Instant closed;
    private State state = State.PREPARED;

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
    }

    public Channel(ChannelId id, Session session, C command) {
        this.id = id;
        this.command = command;
        this.session = session;
        this.decoder = command.createChannelDecoder();

        this.sessionLogPrefix = session.getLogPrefix();
    }

    public C getCommand() {
        return command;
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

            M msg = decoder.decode(channelMessage);
            LOGGER.debug("{}[{}] received: {}", sessionLogPrefix, id, msg);

            ReceivedMessage.Type messageType = msg.getType();

            State newState = state.transition(messageType);
            if (newState != state) {
                LOGGER.debug("{}[{}] command state {} => {}", sessionLogPrefix, id, state, newState);
            }

            if (messageType.opensChannel()) {
                if (confirmed != null) {
                    throw new IllegalArgumentException("Command has already been confirmed; received: " + msg);
                }

                confirmed = msg.getLocalReceiveTimestamp();
            }

            if (messageType.closesChannel()) {
                if (confirmed == null) {
                    confirmed = msg.getLocalReceiveTimestamp();
                }

                closed = msg.getLocalReceiveTimestamp();
            }

            state = newState;
        }
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
