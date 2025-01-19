package de.energiequant.xprc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.energiequant.xprc.types.ValueType;

public class DRLSChannel<SELF extends DRLSChannel<SELF, CFB, C>, CFB extends DRLSChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, DRLSMessage>> extends Channel<SELF, C, DRLSMessage> {
    private final char FIELD_SEPARATOR = ';';

    public DRLSChannel(ChannelId id, Session session, C command) {
        super(id, session, command);
    }

    public List<DataRef<?>> getReceivedDataRefs() {
        // FIXME: implement
        return Collections.emptyList();
    }

    public static class FactoryBuilder<SELF extends FactoryBuilder<SELF, CH, C>, CH extends DRLSChannel<CH, SELF, C>, C extends Command<SELF, CH, C, DRLSMessage>> extends ChannelFactoryBuilder<SELF, CH, C, DRLSMessage> {
        public FactoryBuilder(XPRCClient client, C command) {
            super(client, command);
        }

        @SuppressWarnings("unchecked")
        public SELF onDataRef(Consumer<DataRef<?>> dataRef) {
            return (SELF) this;
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

        return new DRLSMessage(msg, new DRLSMessage.DataRefDescription(name, writable, types));
    }
}
