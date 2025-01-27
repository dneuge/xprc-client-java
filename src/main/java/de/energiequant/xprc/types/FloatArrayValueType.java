package de.energiequant.xprc.types;

class FloatArrayValueType implements ValueType<float[]> {
    public static final String ENCODED_TYPE_NAME = "float[]";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public String serialize(float[] value) {
        return "";
    }

    @Override
    public float[] deserialize(String s) {
        return new float[0];
    }

    @Override
    public String name() {
        return "FLOAT_ARRAY";
    }

    @Override
    public String toString() {
        return name();
    }
}
