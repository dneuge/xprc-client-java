package de.energiequant.xprc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.energiequant.xprc.DRLSMessage.DataRefDescription;
import de.energiequant.xprc.types.ValueType;

public class DRLSChannel<SELF extends DRLSChannel<SELF, CFB, C>, CFB extends DRLSChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, DRLSMessage>> extends Channel<SELF, C, DRLSMessage> {
    private final char FIELD_SEPARATOR = ';';

    private final List<DataRefDescription> receivedDataRefs = new ArrayList<>();
    private final BiConsumer<SELF, DataRefDescription> onDataRef;

    public DRLSChannel(ChannelId id, Session session, C command, Callbacks<SELF, C, DRLSMessage> externalCallbacks, BiConsumer<SELF, DataRefDescription> onDataRef) {
        super(id, session, command, externalCallbacks);
        this.onDataRef = onDataRef;
    }

    public List<DataRefDescription> getReceivedDataRefs() {
        synchronized (receivedDataRefs) {
            return Collections.unmodifiableList(new ArrayList<>(receivedDataRefs));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onDataMessage(DRLSMessage msg) {
        DataRefDescription description = msg.getDataRefDescription()
                                            .orElseThrow(() -> new IllegalArgumentException("provided message does not hold data"));

        synchronized (receivedDataRefs) {
            receivedDataRefs.add(description);
        }

        if (onDataRef != null) {
            try {
                onDataRef.accept((SELF) this, description);
            } catch (Exception ex) {
                XPRCClient client = getSession().getClient();
                XPRCException.sendTo(client.getExceptionHandler(), client, XPRCException.Consequence.DOWNSTREAM, "custom channel callback for onDataRef failed", ex);
            }
        }
    }

    @Override
    protected DRLSMessage decode(ChannelMessage msg) {
        String payload = msg.getRawPayload().orElse(null);
        if (payload == null) {
            return new DRLSMessage(msg);
        }

        int endOfTypes = payload.indexOf(FIELD_SEPARATOR);

        Collection<ValueType<?>> types = Arrays.stream(payload.substring(0, endOfTypes).split(","))
                                               .map(typeName -> ValueType.resolve(typeName)
                                                                         .orElseThrow(
                                                                             () -> new IllegalArgumentException(
                                                                                 "Unknown value type \"" + typeName + "\""
                                                                             )
                                                                         ))
                                               .collect(Collectors.toList());

        int startOfAccess = endOfTypes + 1;
        int endOfAccess = payload.indexOf(FIELD_SEPARATOR, startOfAccess);
        String access = payload.substring(startOfAccess, endOfAccess);
        boolean writable;
        switch (access) {
            case "rw":
                writable = true;
                break;
            case "ro":
                writable = false;
                break;
            default:
                throw new IllegalArgumentException("Unknown access mode: \"" + access + "\"");
        }

        int startOfName = endOfAccess + 1;
        String name = payload.substring(startOfName);

        return new DRLSMessage(msg, new DataRefDescription(name, writable, types));
    }

    public static class FactoryBuilder<SELF extends FactoryBuilder<SELF, CH, C>, CH extends DRLSChannel<CH, SELF, C>, C extends Command<SELF, CH, C, DRLSMessage>> extends ChannelFactoryBuilder<SELF, CH, C, DRLSMessage> {
        private BiConsumer<SELF, DataRefDescription> onDataRef;

        public FactoryBuilder(XPRCClient client, Supplier<C> commandFactory) {
            super(client, commandFactory);
        }

        @Override
        protected ChannelFactory<CH, C, DRLSMessage> buildSpecificFactory(Supplier<C> commandFactory, Callbacks<CH, C, DRLSMessage> externalCallbacks) {
            final BiConsumer<SELF, DataRefDescription> onDataRefCopy = onDataRef;

            return new ChannelFactory<CH, C, DRLSMessage>(commandFactory) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                @Override
                CH createChannel(ChannelId channelId, Session session, C command) {
                    return (CH) new DRLSChannel(channelId, session, command, externalCallbacks, onDataRefCopy);
                }
            };
        }

        @SuppressWarnings("unchecked")
        public SELF onDataRef(BiConsumer<SELF, DataRefDescription> callback) {
            this.onDataRef = callback;
            return (SELF) this;
        }
    }
}
