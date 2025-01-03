package de.energiequant.xprc;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import de.energiequant.xprc.types.ValueType;

public class DRLSDecoder implements ChannelDecoder<DRLSMessage> {
    // FIXME: draft/WIP

    private final char FIELD_SEPARATOR = ';';

    @Override
    public DRLSMessage decode(ChannelMessage msg) {
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
