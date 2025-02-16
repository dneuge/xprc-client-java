package de.energiequant.xprc.types;

class FloatArrayValueType implements ValueType<float[]> {
    // FIXME: implement

    public static final String ENCODED_TYPE_NAME = "float[]";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public String serialize(Object value) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String serialize(int value) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String serialize(float value) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String serialize(double value) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public float[] deserialize(String s) {
        throw new UnsupportedOperationException("not implemented yet");
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
