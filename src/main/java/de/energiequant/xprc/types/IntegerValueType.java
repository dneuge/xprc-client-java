package de.energiequant.xprc.types;

class IntegerValueType implements ValueType<Integer> {
    public static final String ENCODED_TYPE_NAME = "int";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public String serialize(Object value) {
        if (value instanceof Integer) {
            return value.toString();
        }

        if (value instanceof Double || value instanceof Float) {
            throw new IllegalArgumentException("Conversion is not supported yet, got: " + value.getClass().getCanonicalName());
        }

        throw new IllegalArgumentException("Unsupported type: " + value.getClass().getCanonicalName());
    }

    @Override
    public String serialize(int value) {
        return Integer.toString(value);
    }

    @Override
    public String serialize(float value) {
        throw new IllegalArgumentException("Conversion is not supported yet, got: float");
    }

    @Override
    public String serialize(double value) {
        throw new IllegalArgumentException("Conversion is not supported yet, got: double");
    }

    @Override
    public Integer deserialize(String s) {
        return Integer.parseInt(s);
    }

    @Override
    public String name() {
        return "INTEGER";
    }

    @Override
    public String toString() {
        return name();
    }
}
