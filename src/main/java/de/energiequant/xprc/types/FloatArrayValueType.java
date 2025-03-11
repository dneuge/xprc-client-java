package de.energiequant.xprc.types;

class FloatArrayValueType implements ValueType<float[]> {
    // FIXME: implement
    // TODO: unit tests

    public static final String ENCODED_TYPE_NAME = "float[]";

    private static final String SEPARATOR = ",";

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
            sb.append(SEPARATOR);
            sb.append(v);
        }

        return sb.toString();
    }

    @Override
    public String serialize(Object value) {
        if (value instanceof float[]) {
            return serialize((float[]) value);
        } else if (value instanceof Float) {
            return serialize((float) value);
        } else if (value instanceof Double) {
            return serialize((double) value);
        } else if (value instanceof Integer) {
            return serialize((int) value);
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
        String[] split = s.split(",");
        if (split.length < 1) {
            throw new IllegalArgumentException("Bad float[] format: \"" + s + "\"");
        }

        int length = Integer.parseUnsignedInt(split[0]);
        if (length == 0) {
            throw new IllegalArgumentException("Empty float[] not supported: \"" + s + "\"");
        }

        if (length != split.length - 1) {
            throw new IllegalArgumentException("Length mismatch on float[]: \"" + s + "\"");
        }

        float[] out = new float[length];
        for (int i = 0; i < length; i++) {
            out[i] = ValueType.FLOAT.deserialize(split[i + 1]);
        }

        return out;
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
