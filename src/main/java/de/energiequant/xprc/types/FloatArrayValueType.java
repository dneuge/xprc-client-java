package de.energiequant.xprc.types;

class FloatArrayValueType implements ValueType<float[]> {

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
