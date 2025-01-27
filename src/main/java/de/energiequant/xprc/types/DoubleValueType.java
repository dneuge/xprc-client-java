package de.energiequant.xprc.types;

class DoubleValueType implements ValueType<Double> {
    public static final String ENCODED_TYPE_NAME = "double";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public String serialize(Double value) {
        return value.toString();
    }

    @Override
    public Double deserialize(String s) {
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
