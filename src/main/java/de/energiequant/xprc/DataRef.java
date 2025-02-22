package de.energiequant.xprc;

import java.util.Optional;

import de.energiequant.xprc.types.ValueType;

public class DataRef<T> {
    // FIXME: draft/WIP - do we need support for DataRefs of unknown type? how to represent?

    private final ValueType<T> type;
    private final String name;
    private final int arrayLength;

    private DataRef(ValueType<T> type, String name) {
        this.type = type;
        this.name = name;
        this.arrayLength = -1;
    }

    private DataRef(ValueType<T> type, String name, int arrayLength) {
        this.type = type;
        this.name = name;
        this.arrayLength = arrayLength;

        if (!type.isArray()) {
            throw new IllegalArgumentException("Array length was specified for non-array type " + type);
        }

        if (arrayLength < 0) {
            throw new IllegalArgumentException("Array length must be positive (incl. zero), got: " + arrayLength);
        }
    }

    public String getName() {
        return name;
    }

    public ValueType<T> getType() {
        return type;
    }

    public Optional<Integer> getArrayLength() {
        if (arrayLength < 0) {
            return Optional.empty();
        }

        return Optional.of(arrayLength);
    }

    public static <U> DataRef<U> of(ValueType<U> type, String name) {
        return new DataRef<>(type, name);
    }

    public static <U> DataRef<U> withArrayLength(ValueType<U> type, String name, int arrayLength) {
        return new DataRef<>(type, name, arrayLength);
    }

    @Override
    public String toString() {
        return "DataRef("
            + type.getEncodedTypeName() + ":"
            + name + ")";
    }
}
