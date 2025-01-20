package de.energiequant.xprc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.energiequant.xprc.types.ValueType;

public class DRLSMessage extends ChannelMessage {
    // FIXME: draft/WIP

    private final DataRefDescription dataRefDescription;

    public static class DataRefDescription {
        private final String name;
        private final boolean writable;
        private final Set<ValueType<?>> types;

        public DataRefDescription(String name, boolean writable, Collection<ValueType<?>> types) {
            this.name = name;
            this.writable = writable;
            this.types = Collections.unmodifiableSet(new HashSet<>(types));
        }

        public Set<ValueType<?>> getTypes() {
            return types;
        }

        public String getName() {
            return name;
        }

        public boolean isWritable() {
            return writable;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("DataRefDescription(\"");
            sb.append(name);
            sb.append(", writable=");
            sb.append(writable);
            sb.append(", types={");
            boolean isFirst = true;
            for (ValueType type : types) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(", ");
                }
                
                sb.append(type);
            }
            sb.append("})");

            return sb.toString();
        }
    }

    DRLSMessage(ChannelMessage msg) {
        super(msg);
        this.dataRefDescription = null;
    }

    DRLSMessage(ChannelMessage msg, DataRefDescription dataRefDescription) {
        super(msg);
        this.dataRefDescription = dataRefDescription;
    }

    public Optional<DataRefDescription> getDataRefDescription() {
        return Optional.ofNullable(dataRefDescription);
    }

    @Override
    public boolean containsData() {
        return dataRefDescription != null;
    }

    @Override
    protected void toString(StringBuilder sb) {
        sb.append(", ");
        sb.append(dataRefDescription);
    }
}
