package de.energiequant.xprc.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.energiequant.xprc.ChannelMessage;
import de.energiequant.xprc.DataRef;

public class DRQVMessage extends ChannelMessage {
    private final DataRef<?>[] dataRefs;
    private final String[] encodedValues;

    DRQVMessage(ChannelMessage msg) {
        super(msg);
        this.dataRefs = null;
        this.encodedValues = null;
    }

    DRQVMessage(ChannelMessage msg, DataRef<?>[] dataRefs, String[] encodedValues) {
        super(msg);

        if (encodedValues.length != dataRefs.length) {
            throw new IllegalArgumentException("payload holds " + encodedValues.length + " values, expected " + dataRefs.length);
        }

        this.dataRefs = dataRefs;
        this.encodedValues = encodedValues;
    }

    public <T> Optional<T> getValue(DataRef<T> wantedDataRef) {
        if (dataRefs == null || encodedValues == null) {
            return Optional.empty();
        }

        wantedDataRef = wantedDataRef.withoutArrayLength();

        for (int i = 0; i < dataRefs.length; i++) {
            DataRef<?> dataRef = dataRefs[i];
            if (dataRef.equals(wantedDataRef)) {
                return Optional.of(wantedDataRef.getType().deserialize(encodedValues[i]));
            }
        }

        throw new IllegalArgumentException(
            "Wanted " + wantedDataRef + " is not handled by this DRQV channel; available: "
                + Arrays.stream(dataRefs)
                        .map(DataRef::toString)
                        .collect(Collectors.joining(", "))
        );
    }

    public Map<DataRef<?>, Object> getAllValues() {
        if (dataRefs == null || encodedValues == null) {
            return Collections.emptyMap();
        }

        Map<DataRef<?>, Object> out = new LinkedHashMap<>();

        for (int i = 0; i < dataRefs.length; i++) {
            DataRef<?> dataRef = dataRefs[i];
            out.put(dataRef, dataRef.getType().deserialize(encodedValues[i]));
        }

        return out;
    }

    @Override
    public boolean containsData() {
        return dataRefs != null;
    }

    @Override
    protected void toString(StringBuilder sb) {
        if (dataRefs == null || encodedValues == null) {
            return;
        }

        for (int i = 0; i < dataRefs.length; i++) {
            sb.append(", ");
            sb.append(dataRefs[i]);
            sb.append("=");
            sb.append(encodedValues[i]);
        }
    }
}
