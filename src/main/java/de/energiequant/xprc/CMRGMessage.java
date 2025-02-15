package de.energiequant.xprc;

import java.util.Optional;

public class CMRGMessage extends ChannelMessage {
    // FIXME: draft/WIP

    private final Event event;

    public enum Event {
        HOLD, //
        TRIGGER, //
        RELEASE;
    }

    CMRGMessage(ChannelMessage msg) {
        super(msg);
        this.event = null;
    }

    CMRGMessage(ChannelMessage msg, Event event) {
        super(msg);
        this.event = event;
    }

    public Optional<Event> getEvent() {
        return Optional.ofNullable(event);
    }

    @Override
    public boolean containsData() {
        return event != null;
    }

    @Override
    protected void toString(StringBuilder sb) {
        sb.append(", ");
        sb.append(event);
    }
}
