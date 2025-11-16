package de.energiequant.xprc.types;

class DoubleValueType implements ValueType<Double> {
    public static final String ENCODED_TYPE_NAME = "double";

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
        if (value instanceof Double || value instanceof Float || value instanceof Integer) {
            return value.toString();
        }

        throw new IllegalArgumentException("Unsupported type: " + value.getClass().getCanonicalName());
    }

    @Override
    public String serialize(int value) {
        return Integer.toString(value);
    }

    @Override
    public String serialize(float value) {
        return Float.toString(value);
    }

    @Override
    public String serialize(double value) {
        return Double.toString(value);
    }

    @Override
    public Double deserialize(String s) {
        if ("nan".equals(s)) {
            return Double.NaN;
        }

        return Double.parseDouble(s);
    }

    @Override
    public String name() {
        return "DOUBLE";
    }

    @Override
    public String toString() {
        return name();
    }
}
