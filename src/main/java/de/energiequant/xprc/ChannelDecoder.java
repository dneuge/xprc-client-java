package de.energiequant.xprc;

public interface ChannelDecoder<T extends ChannelMessage> {
    // FIXME: draft/WIP

    T decode(ChannelMessage msg);
}
