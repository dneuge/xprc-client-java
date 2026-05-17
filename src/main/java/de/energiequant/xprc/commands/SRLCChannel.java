package de.energiequant.xprc.commands;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import de.energiequant.xprc.Channel;
import de.energiequant.xprc.ChannelFactory;
import de.energiequant.xprc.ChannelFactoryBuilder;
import de.energiequant.xprc.ChannelId;
import de.energiequant.xprc.ChannelMessage;
import de.energiequant.xprc.Command;
import de.energiequant.xprc.Session;
import de.energiequant.xprc.XPRCClient;

public class SRLCChannel<SELF extends SRLCChannel<SELF, CFB, C>, CFB extends SRLCChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, SRLCMessage>> extends Channel<SELF, C, SRLCMessage> {
    public SRLCChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, SRLCMessage> externalCallbacks) {
        super(id, session, command, externalCallbacks);
    }

    @Override
    protected SRLCMessage decode(ChannelMessage msg) {
        return new SRLCMessage(msg);
    }

    public static class FactoryBuilder<SELF extends SRLCChannel.FactoryBuilder<SELF, CH, C>, CH extends SRLCChannel<CH, SELF, C>, C extends Command<SELF, CH, C, SRLCMessage>> extends ChannelFactoryBuilder<SELF, CH, C, SRLCMessage> {

        protected FactoryBuilder(XPRCClient client, Supplier<C> commandFactory) {
            super(client, commandFactory);
        }

        @Override
        protected ChannelFactory<CH, C, SRLCMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, SRLCMessage> externalCallbacks) {
            return new ChannelFactory<CH, C, SRLCMessage>(commandFactory) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                protected CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new SRLCChannel(channelId, session, command, externalCallbacks);
                }
            };
        }

        public CompletableFuture<SRLCAggregator.SRLCResult> submitAndAggregate() {
            return SRLCAggregator.submitCommand(this);
        }
    }
}
