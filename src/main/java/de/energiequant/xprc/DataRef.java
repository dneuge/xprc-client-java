package de.energiequant.xprc;

import java.util.Objects;
import java.util.Optional;

import de.energiequant.xprc.types.ValueType;

public class DataRef<T> {
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

    public DataRef<T> withoutArrayLength() {
        if (arrayLength < 0) {
            return this;
        }

        return new DataRef<>(type, name);
    }

    public static <U> DataRef<U> of(ValueType<U> type, String name) {
        return new DataRef<>(type, name);
    }

    public static <U> DataRef<U> withArrayLength(ValueType<U> type, String name, int arrayLength) {
        return new DataRef<>(type, name, arrayLength);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof DataRef)) {
            return false;
        }

        DataRef<?> other = (DataRef<?>) obj;

        return this.type == other.type
            && this.arrayLength == other.arrayLength
            && this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, arrayLength, name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DataRef(");

        sb.append(type.getEncodedTypeName());
        sb.append(":");
        sb.append(name);

        if (arrayLength >= 0) {
            sb.append(", arrayLength=");
            sb.append(arrayLength);
        }

        sb.append(")");

        return sb.toString();
    }
}
