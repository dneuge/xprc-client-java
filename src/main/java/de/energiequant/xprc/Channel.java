package de.energiequant.xprc;

import java.time.Instant;

public class Channel {
    // FIXME: draft/WIP

    ChannelId id;
    Command command;
    Session session;

    Instant dispatched;
    Instant confirmed;
    Instant closed;
    CommandState commandState;

    public enum CommandState {
        DISPATCHED, ACKNOWLEDGED, FINISHED, ERROR;
    }

    public void close() {
        // FIXME: implement - send termination request via Session if not already closed
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Channel(");

        sb.append(id);
        sb.append("/");
        sb.append(commandState);
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
