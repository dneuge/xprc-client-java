package de.energiequant.xprc.commands;

import java.util.List;
import java.util.function.Supplier;

import de.energiequant.xprc.Channel;
import de.energiequant.xprc.ChannelFactory;
import de.energiequant.xprc.ChannelFactoryBuilder;
import de.energiequant.xprc.ChannelId;
import de.energiequant.xprc.ChannelMessage;
import de.energiequant.xprc.Command;
import de.energiequant.xprc.Session;
import de.energiequant.xprc.XPRCClient;
import de.energiequant.xprc.types.ValueType;

public class DRCIChannel<SELF extends DRCIChannel<SELF, CFB, C>, CFB extends DRCIChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, DRCIMessage>> extends Channel<SELF, C, DRCIMessage> {
    private final ValueType<?>[] types;

    private static final String VALUE_SEPARATOR = ";";

    public DRCIChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, DRCIMessage> externalCallbacks, List<ValueType<?>> types) {
        super(id, session, command, externalCallbacks);

        this.types = types.toArray(new ValueType[0]);
    }

    @Override
    protected DRCIMessage decode(ChannelMessage msg) {
        String payload = msg.getRawPayload().orElse(null);
        if (payload == null) {
            return new DRCIMessage(msg);
        }

        String[] encodedValues = payload.split(VALUE_SEPARATOR);

        return new DRCIMessage(msg, types, encodedValues);
    }

    public static class FactoryBuilder<SELF extends DRCIChannel.FactoryBuilder<SELF, CH, C>, CH extends DRCIChannel<CH, SELF, C>, C extends Command<SELF, CH, C, DRCIMessage>> extends ChannelFactoryBuilder<SELF, CH, C, DRCIMessage> {
        private final List<ValueType<?>> types;

        protected FactoryBuilder(XPRCClient client, Supplier<C> commandFactory, List<ValueType<?>> types) {
            super(client, commandFactory);
            this.types = types;
        }

        @Override
        protected ChannelFactory<CH, C, DRCIMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, DRCIMessage> externalCallbacks) {
            return new ChannelFactory<CH, C, DRCIMessage>(commandFactory) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                protected CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new DRCIChannel(channelId, session, command, externalCallbacks, types);
                }
            };
        }
    }
}
