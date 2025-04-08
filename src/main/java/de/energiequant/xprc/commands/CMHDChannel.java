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

public class CMHDChannel<SELF extends CMHDChannel<SELF, CFB, C>, CFB extends CMHDChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, CMHDMessage>> extends Channel<SELF, C, CMHDMessage> {
    public CMHDChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, CMHDMessage> externalCallbacks) {
        super(id, session, command, externalCallbacks);
    }

    @Override
    protected CMHDMessage decode(ChannelMessage msg) {
        return new CMHDMessage(msg);
    }

    public static class FactoryBuilder<SELF extends CMHDChannel.FactoryBuilder<SELF, CH, C>, CH extends CMHDChannel<CH, SELF, C>, C extends Command<SELF, CH, C, CMHDMessage>> extends ChannelFactoryBuilder<SELF, CH, C, CMHDMessage> {

        protected FactoryBuilder(XPRCClient client, Supplier<C> commandFactory) {
            super(client, commandFactory);
        }

        @Override
        protected ChannelFactory<CH, C, CMHDMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, CMHDMessage> externalCallbacks) {
            return new ChannelFactory<CH, C, CMHDMessage>(commandFactory) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                protected CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new CMHDChannel(channelId, session, command, externalCallbacks);
                }
            };
        }
    }
}
