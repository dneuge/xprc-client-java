package de.energiequant.xprc.types;

class BlobValueType implements ValueType<byte[]> {
    // FIXME: implement

    public static final String ENCODED_TYPE_NAME = "blob";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public String serialize(Object value) {
        /*
        StringBuilder sb = new StringBuilder(Integer.toString(value.length));

        return sb.toString();
        */

        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String serialize(int value) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String serialize(float value) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String serialize(double value) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public byte[] deserialize(String s) {
        throw new UnsupportedOperationException("not implemented yet");
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
