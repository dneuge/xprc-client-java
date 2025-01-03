package de.energiequant.xprc.types;

class IntegerValueType implements ValueType<Integer> {

    @Override
    public String serialize(Integer value) {
        return value.toString();
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
