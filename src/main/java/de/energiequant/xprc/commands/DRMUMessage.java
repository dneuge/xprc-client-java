package de.energiequant.xprc.commands;

import de.energiequant.xprc.ChannelMessage;

public class DRMUMessage extends ChannelMessage {
    // FIXME: implement parsing of monitored values (reuse DRQV?)

    DRMUMessage(ChannelMessage msg) {
        super(msg);
    }
}
