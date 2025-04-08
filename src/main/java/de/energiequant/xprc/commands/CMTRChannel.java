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

public class CMTRChannel<SELF extends CMTRChannel<SELF, CFB, C>, CFB extends CMTRChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, CMTRMessage>> extends Channel<SELF, C, CMTRMessage> {
    public CMTRChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, CMTRMessage> externalCallbacks) {
        super(id, session, command, externalCallbacks);
    }

    @Override
    protected CMTRMessage decode(ChannelMessage msg) {
        return new CMTRMessage(msg);
    }

    public static class FactoryBuilder<SELF extends CMTRChannel.FactoryBuilder<SELF, CH, C>, CH extends CMTRChannel<CH, SELF, C>, C extends Command<SELF, CH, C, CMTRMessage>> extends ChannelFactoryBuilder<SELF, CH, C, CMTRMessage> {

        protected FactoryBuilder(XPRCClient client, Supplier<C> commandFactory) {
            super(client, commandFactory);
        }

        @Override
        protected ChannelFactory<CH, C, CMTRMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, CMTRMessage> externalCallbacks) {
            return new ChannelFactory<CH, C, CMTRMessage>(commandFactory) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                protected CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new CMTRChannel(channelId, session, command, externalCallbacks);
                }
            };
        }
    }
}
