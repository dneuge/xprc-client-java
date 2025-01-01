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

    }
}
