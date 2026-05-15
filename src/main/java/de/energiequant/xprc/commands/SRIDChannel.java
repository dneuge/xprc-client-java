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
import de.energiequant.xprc.commands.SRIDAggregator.SRIDResult;

public class SRIDChannel<SELF extends SRIDChannel<SELF, CFB, C>, CFB extends SRIDChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, SRIDMessage>> extends Channel<SELF, C, SRIDMessage> {
    public SRIDChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, SRIDMessage> externalCallbacks) {
        super(id, session, command, externalCallbacks);
    }

    @Override
    protected SRIDMessage decode(ChannelMessage msg) {
        return new SRIDMessage(msg);
    }

    public static class FactoryBuilder<SELF extends SRIDChannel.FactoryBuilder<SELF, CH, C>, CH extends SRIDChannel<CH, SELF, C>, C extends Command<SELF, CH, C, SRIDMessage>> extends ChannelFactoryBuilder<SELF, CH, C, SRIDMessage> {
        protected FactoryBuilder(XPRCClient client, Supplier<C> commandFactory) {
            super(client, commandFactory);
        }

        @Override
        protected ChannelFactory<CH, C, SRIDMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, SRIDMessage> externalCallbacks) {
            return new ChannelFactory<CH, C, SRIDMessage>(commandFactory) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                protected CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new SRIDChannel(channelId, session, command, externalCallbacks);
                }
            };
        }

        public CompletableFuture<SRIDResult> submitAndAggregate() {
            return SRIDAggregator.submitCommand(this);
        }
    }
}
