package de.energiequant.xprc.types;

class BlobValueType implements ValueType<byte[]> {
    // FIXME: implement

    public static final String ENCODED_TYPE_NAME = "blob";

    @Override
    public String getEncodedTypeName() {
        return ENCODED_TYPE_NAME;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public String serialize(byte[] value) {
        StringBuilder sb = new StringBuilder(Integer.toString(value.length));
        sb.append(",");

        for (byte b : value) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    @Override
    public String serialize(Object value) {
        if (!(value instanceof byte[])) {
            throw new IllegalArgumentException("only byte[] serialization is supported for blobs");
        }

        return serialize((byte[]) value);
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
