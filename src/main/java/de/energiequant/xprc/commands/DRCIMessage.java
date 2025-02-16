package de.energiequant.xprc.commands;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import de.energiequant.xprc.ChannelMessage;
import de.energiequant.xprc.types.ValueType;

public class DRCIMessage extends ChannelMessage {
    // FIXME: draft/WIP

    private final ValueType<?>[] types;
    private final String[] encodedValues;

    DRCIMessage(ChannelMessage msg) {
        super(msg);
        this.types = null;
        this.encodedValues = null;
    }

    DRCIMessage(ChannelMessage msg, ValueType<?>[] types, String[] encodedValues) {
        super(msg);

        if (encodedValues.length != types.length) {
            throw new IllegalArgumentException("payload holds " + encodedValues.length + " values, expected " + types.length);
        }

        this.types = types;
        this.encodedValues = encodedValues;
    }

    public <T> Optional<T> getValue(ValueType<T> wantedType) {
        if (types == null || encodedValues == null) {
            return Optional.empty();
        }

        for (int i = 0; i < types.length; i++) {
            ValueType<?> type = types[i];
            if (type == wantedType) {
                return Optional.of(wantedType.deserialize(encodedValues[i]));
            }
        }

        throw new IllegalArgumentException(
            "Wanted type " + wantedType + " is not handled by this DRCI channel; available: "
                + Arrays.stream(types)
                        .map(ValueType::name)
                        .collect(Collectors.joining(", "))
        );
    }

    @Override
    public boolean containsData() {
        return types != null;
    }

    @Override
    protected void toString(StringBuilder sb) {
        if (types == null || encodedValues == null) {
            return;
        }

        sb.append(", ");

        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append('[');
            sb.append(types[i].getEncodedTypeName());
            sb.append(']');
            sb.append(encodedValues[i]);
        }
    }
}
