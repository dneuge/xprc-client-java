package de.energiequant.xprc.commands;

import java.util.Optional;

import de.energiequant.xprc.ChannelMessage;

public class CMRGMessage extends ChannelMessage {
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
