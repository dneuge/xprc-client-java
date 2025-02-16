package de.energiequant.xprc.types;

class FloatValueType implements ValueType<Float> {
    public static final String ENCODED_TYPE_NAME = "float";

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
        if (value instanceof Float || value instanceof Integer) {
            return value.toString();
        }

        if (value instanceof Double) {
            return serialize(((Double) value).floatValue());
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
        return serialize((float) value);
    }

    @Override
    public Float deserialize(String s) {
        return Float.parseFloat(s);
    }

    @Override
    public String name() {
        return "FLOAT";
    }

    @Override
    public String toString() {
        return name();
    }
}
