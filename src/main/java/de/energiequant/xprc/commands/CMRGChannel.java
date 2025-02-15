package de.energiequant.xprc.commands;

import java.util.function.Supplier;

import de.energiequant.xprc.Channel;
import de.energiequant.xprc.ChannelFactory;
import de.energiequant.xprc.ChannelFactoryBuilder;
import de.energiequant.xprc.ChannelId;
import de.energiequant.xprc.ChannelMessage;
import de.energiequant.xprc.Command;
import de.energiequant.xprc.Session;
import de.energiequant.xprc.XPRCClient;

public class CMRGChannel<SELF extends CMRGChannel<SELF, CFB, C>, CFB extends CMRGChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, CMRGMessage>> extends Channel<SELF, C, CMRGMessage> {
    public CMRGChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, CMRGMessage> externalCallbacks) {
        super(id, session, command, externalCallbacks);
    }

    @Override
    protected CMRGMessage decode(ChannelMessage msg) {
        String payload = msg.getRawPayload().orElse(null);
        if (payload == null) {
            return new CMRGMessage(msg);
        }

        return new CMRGMessage(msg, decodeEvent(payload));
    }

    private CMRGMessage.Event decodeEvent(String payload) {
        for (CMRGMessage.Event event : CMRGMessage.Event.values()) {
            if (event.name().equals(payload)) {
                return event;
            }
        }

        throw new IllegalArgumentException("Unhandled event: " + payload);
    }

    public static class FactoryBuilder<SELF extends CMRGChannel.FactoryBuilder<SELF, CH, C>, CH extends CMRGChannel<CH, SELF, C>, C extends Command<SELF, CH, C, CMRGMessage>> extends ChannelFactoryBuilder<SELF, CH, C, CMRGMessage> {
        protected FactoryBuilder(XPRCClient client, Supplier<C> commandFactory) {
            super(client, commandFactory);
        }

        @Override
        protected ChannelFactory<CH, C, CMRGMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, CMRGMessage> externalCallbacks) {
            return new ChannelFactory<CH, C, CMRGMessage>(commandFactory) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                protected CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new CMRGChannel(channelId, session, command, externalCallbacks);
                }
            };
        }
    }
}
