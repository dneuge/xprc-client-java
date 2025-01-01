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
}
