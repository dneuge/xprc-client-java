package de.energiequant.xprc.types;

class DoubleValueType implements ValueType<Double> {
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
