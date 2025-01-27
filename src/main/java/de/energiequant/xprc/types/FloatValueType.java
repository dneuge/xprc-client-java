package de.energiequant.xprc.types;

class FloatValueType implements ValueType<Float> {
    public static final String ENCODED_TYPE_NAME = "float";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public String serialize(Float value) {
        return value.toString();
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
