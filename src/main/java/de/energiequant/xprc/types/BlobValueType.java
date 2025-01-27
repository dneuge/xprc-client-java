package de.energiequant.xprc.types;

class BlobValueType implements ValueType<byte[]> {
    public static final String ENCODED_TYPE_NAME = "blob";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public String serialize(byte[] value) {
        StringBuilder sb = new StringBuilder(Integer.toString(value.length));

        return sb.toString();
    }

    @Override
    public byte[] deserialize(String s) {
        return new byte[0];
    }

    @Override
    public String name() {
        return "BLOB";
    }

    @Override
    public String toString() {
        return name();
    }
}
