package de.energiequant.xprc.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import de.energiequant.xprc.Channel;
import de.energiequant.xprc.ChannelFactory;
import de.energiequant.xprc.ChannelFactoryBuilder;
import de.energiequant.xprc.ChannelId;
import de.energiequant.xprc.ChannelMessage;
import de.energiequant.xprc.Command;
import de.energiequant.xprc.DataRef;
import de.energiequant.xprc.Session;
import de.energiequant.xprc.XPRCClient;

public class DRQVChannel<SELF extends DRQVChannel<SELF, CFB, C>, CFB extends DRQVChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, DRQVMessage>> extends Channel<SELF, C, DRQVMessage> {
    private final DataRef<?>[] dataRefs;

    @SuppressWarnings("rawtypes")
    private final Map<DataRef, ValueCallback> valueCallbacks;

    private static final String VALUE_SEPARATOR = ";";

    @FunctionalInterface
    public interface ValueCallback<T> {
        void onValue(DRQVChannel channel, DRQVMessage message, DataRef<T> dataRef, T value);
    }

    public DRQVChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, DRQVMessage> externalCallbacks, List<DataRef<?>> dataRefs, @SuppressWarnings("rawtypes") Map<DataRef, ValueCallback> valueCallbacks) {
        super(id, session, command, externalCallbacks);

        this.dataRefs = dataRefs.toArray(new DataRef[0]);
        this.valueCallbacks = valueCallbacks;
    }

    @Override
    protected DRQVMessage decode(ChannelMessage msg) {
        String payload = msg.getRawPayload().orElse(null);
        if (payload == null) {
            return new DRQVMessage(msg);
        }

        String[] encodedValues = payload.split(VALUE_SEPARATOR);

        return new DRQVMessage(msg, dataRefs, encodedValues);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void onDataMessage(DRQVMessage msg) {
        if (!msg.containsData() || valueCallbacks.isEmpty()) {
            return;
        }

        Map<DataRef<?>, Object> values = msg.getAllValues();
        for (Map.Entry<DataRef<?>, Object> entry : values.entrySet()) {
            DataRef<?> dataRef = entry.getKey();

            ValueCallback callback = valueCallbacks.get(dataRef);
            if (callback == null) {
                continue;
            }

            callback.onValue(this, msg, dataRef, entry.getValue());
        }
    }

    public static class FactoryBuilder<SELF extends DRQVChannel.FactoryBuilder<SELF, CH, C>, CH extends DRQVChannel<CH, SELF, C>, C extends Command<SELF, CH, C, DRQVMessage>> extends ChannelFactoryBuilder<SELF, CH, C, DRQVMessage> {
        private final List<DataRef<?>> dataRefs;

        @SuppressWarnings("rawtypes")
        private final Map<DataRef, ValueCallback> valueCallbacks;

        protected FactoryBuilder(XPRCClient client, Supplier<C> commandFactory, List<DataRef<?>> dataRefs, @SuppressWarnings("rawtypes") Map<DataRef, ValueCallback> valueCallbacks) {
            super(client, commandFactory);
            this.dataRefs = new ArrayList<>(dataRefs);
            this.valueCallbacks = new HashMap<>(valueCallbacks);
        }

        @Override
        protected ChannelFactory<CH, C, DRQVMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, DRQVMessage> externalCallbacks) {
            return new ChannelFactory<CH, C, DRQVMessage>(commandFactory) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                protected CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new DRQVChannel(channelId, session, command, externalCallbacks, dataRefs, valueCallbacks);
                }
            };
        }
    }
}
