package de.energiequant.xprc.types;

class IntegerArrayValueType implements ValueType<int[]> {
    public static final String ENCODED_TYPE_NAME = "int[]";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public String serialize(int[] value) {
        StringBuilder sb = new StringBuilder(Integer.toString(value.length));

        for (int v : value) {
            sb.append(",");
            sb.append(v);
        }

        return sb.toString();
    }

    @Override
    public String serialize(Object value) {
        if (value instanceof int[]) {
            return serialize((int[]) value);
        }

        if (value instanceof float[] || value instanceof double[]) {
            throw new IllegalArgumentException("Conversion is not supported yet, got: " + value.getClass().getCanonicalName());
        }

        throw new IllegalArgumentException("Unsupported type: " + value.getClass().getCanonicalName());
    }

    @Override
    public String serialize(int value) {
        throw new IllegalArgumentException("Unsupported type: int");
    }

    @Override
    public String serialize(float value) {
        throw new IllegalArgumentException("Unsupported type: float");
    }

    @Override
    public String serialize(double value) {
        throw new IllegalArgumentException("Unsupported type: double");
    }

    @Override
    public int[] deserialize(String s) {
        String[] split = s.split(",");
        if (split.length < 1) {
            throw new IllegalArgumentException("Bad int[] format: \"" + s + "\"");
        }

        int length = Integer.parseUnsignedInt(split[0]);
        if (length == 0) {
            throw new IllegalArgumentException("Empty int[] not supported: \"" + s + "\"");
        }

        if (length != split.length - 1) {
            throw new IllegalArgumentException("Length mismatch on int[]: \"" + s + "\"");
        }

        int[] out = new int[length];
        for (int i = 0; i < length; i++) {
            out[i] = ValueType.INTEGER.deserialize(split[i + 1]);
        }

        return out;
    }

    @Override
    public String name() {
        return "INTEGER_ARRAY";
    }

    @Override
    public String toString() {
        return name();
    }
}
