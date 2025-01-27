package de.energiequant.xprc;

import java.util.function.Supplier;

public class DRMUChannel<SELF extends DRMUChannel<SELF, CFB, C>, CFB extends DRMUChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, DRMUMessage>> extends Channel<SELF, C, DRMUMessage> {
    public DRMUChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, DRMUMessage> externalCallbacks) {
        super(id, session, command, externalCallbacks);
    }

    @Override
    protected DRMUMessage decode(ChannelMessage msg) {
        return new DRMUMessage(msg);
    }

    public static class FactoryBuilder<SELF extends DRMUChannel.FactoryBuilder<SELF, CH, C>, CH extends DRMUChannel<CH, SELF, C>, C extends Command<SELF, CH, C, DRMUMessage>> extends ChannelFactoryBuilder<SELF, CH, C, DRMUMessage> {

        protected FactoryBuilder(XPRCClient client, Supplier<C> commandFactory) {
            super(client, commandFactory);
        }

        @Override
        protected ChannelFactory<CH, C, DRMUMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, DRMUMessage> externalCallbacks) {
            return new ChannelFactory<CH, C, DRMUMessage>(commandFactory) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new DRMUChannel(channelId, session, command, externalCallbacks);
                }
            };
        }
    }
}
