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
        String[] split = s.split(",");
        if (split.length != 2) {
            throw new IllegalArgumentException("Bad blob format: \"" + s + "\"");
        }

        int numBytes = Integer.parseUnsignedInt(split[0]);
        if (numBytes == 0) {
            throw new IllegalArgumentException("Empty blob not supported: \"" + s + "\"");
        }

        char[] sequence = split[1].toCharArray();
        if (sequence.length != numBytes * 2) {
            throw new IllegalArgumentException("Length mismatch on blob: \"" + s + "\"");
        }

        byte[] out = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            int value = (decodeNibble(sequence[i * 2]) << 4) | decodeNibble(sequence[(i * 2) + 1]);
            out[i] = (value < 128) ? (byte) value : (byte) (value - 256);
        }

        return out;
    }

    private static int decodeNibble(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }

        if (ch >= 'A' && ch <= 'F') {
            return ch - 'A' + 10;
        }

        throw new IllegalArgumentException("Invalid nibble character: '" + ch + "' (" + Character.getNumericValue(ch) + ")");
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
