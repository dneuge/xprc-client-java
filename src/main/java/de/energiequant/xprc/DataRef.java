package de.energiequant.xprc;

import de.energiequant.xprc.types.ValueType;

public class DataRef<T> {
    // FIXME: draft/WIP - do we need support for DataRefs of unknown type? how to represent?

    private final ValueType<T> type;
    private final String name;

    private DataRef(ValueType<T> type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ValueType<T> getType() {
        return type;
    }

    public static <U> DataRef<U> of(ValueType<U> type, String name) {
        return new DataRef<>(type, name);
    }

    @Override
    public String toString() {
        return "DataRef("
            + type.getEncodedTypeName() + ":"
            + name + ")";
    }
}
