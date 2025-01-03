package de.energiequant.xprc.types;

import java.util.Optional;

public interface ValueType<T> {
    ValueType<Integer> INTEGER = new IntegerValueType();
    ValueType<Float> FLOAT = new FloatValueType();
    ValueType<Double> DOUBLE = new DoubleValueType();
    ValueType<int[]> INTEGER_ARRAY = new IntegerArrayValueType();
    ValueType<float[]> FLOAT_ARRAY = new FloatArrayValueType();
    ValueType<byte[]> BLOB = new BlobValueType();

    String name();

    String serialize(T value);

    T deserialize(String s);

    static Optional<ValueType<?>> resolve(String encodedTypeName) {
        switch (encodedTypeName) {
            case "int":
                return Optional.of(INTEGER);

            case "float":
                return Optional.of(FLOAT);

            case "double":
                return Optional.of(DOUBLE);

            case "int[]":
                return Optional.of(INTEGER_ARRAY);

            case "float[]":
                return Optional.of(FLOAT_ARRAY);

            case "blob":
                return Optional.of(BLOB);

            default:
                return Optional.empty();
        }
    }
}
