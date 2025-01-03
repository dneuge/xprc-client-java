package de.energiequant.xprc.types;

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
}
