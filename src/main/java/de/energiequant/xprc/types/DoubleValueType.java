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
}
