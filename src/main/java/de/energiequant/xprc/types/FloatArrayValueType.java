package de.energiequant.xprc.types;

class FloatArrayValueType implements ValueType<float[]> {
    // FIXME: implement
    // TODO: unit tests

    public static final String ENCODED_TYPE_NAME = "float[]";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public String serialize(float[] value) {
        StringBuilder sb = new StringBuilder(Integer.toString(value.length));

        for (float v : value) {
            sb.append(",");
            sb.append(v);
        }

        return sb.toString();
    }

    @Override
    public String serialize(Object value) {
        if (value instanceof float[]) {
            return serialize((float[]) value);
        }

        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String serialize(int value) {
        return "1," + ValueType.FLOAT.serialize(value);
    }

    @Override
    public String serialize(float value) {
        return "1," + ValueType.FLOAT.serialize(value);
    }

    @Override
    public String serialize(double value) {
        return "1," + ValueType.FLOAT.serialize(value);
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
