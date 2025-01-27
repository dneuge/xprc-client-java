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

    String getEncodedTypeName();

    T deserialize(String s);

    static Optional<ValueType<?>> resolve(String encodedTypeName) {
        switch (encodedTypeName) {
            case IntegerValueType.ENCODED_TYPE_NAME:
                return Optional.of(INTEGER);

            case FloatValueType.ENCODED_TYPE_NAME:
                return Optional.of(FLOAT);

            case DoubleValueType.ENCODED_TYPE_NAME:
                return Optional.of(DOUBLE);

            case IntegerArrayValueType.ENCODED_TYPE_NAME:
                return Optional.of(INTEGER_ARRAY);

            case FloatArrayValueType.ENCODED_TYPE_NAME:
                return Optional.of(FLOAT_ARRAY);

            case BlobValueType.ENCODED_TYPE_NAME:
                return Optional.of(BLOB);

            default:
                return Optional.empty();
        }
    }
}
