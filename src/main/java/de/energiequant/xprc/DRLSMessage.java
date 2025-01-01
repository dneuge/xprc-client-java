package de.energiequant.xprc;

import java.util.Optional;
import java.util.Set;

import de.energiequant.xprc.types.ValueType;

public class DRLSMessage extends ChannelMessage {
    // FIXME: draft/WIP

    public enum Access {
        READ_ONLY, WRITABLE;
    }

    public static class DataRefDescription {
        public Set<ValueType> getTypes() {
            return null;
        }

        public String getName() {
            return null;
        }

        public Access getAccess() {
            return null;
        }
    }

    DRLSMessage(ChannelMessage msg) {
        super(msg);
    }

    public Optional<DataRefDescription> getDataRefDescription() {
        return null;
    }
}
