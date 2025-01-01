package de.energiequant.xprc;

import java.util.Optional;

public class CMRGMessage extends ChannelMessage {
    // FIXME: draft/WIP
    
    private final Optional<Event> event;

    public enum Event {
        HOLD, //
        TRIGGERED, //
        RELEASE;
    }

    private CMRGMessage(ChannelMessage msg, Optional<Event> event) {
        super(msg);
        this.event = event;
    }

    public Optional<Event> getEvent() {
        return event;
    }
}
