package de.energiequant.xprc.types;

class IntegerValueType implements ValueType<Integer> {
    public static final String ENCODED_TYPE_NAME = "int";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

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
