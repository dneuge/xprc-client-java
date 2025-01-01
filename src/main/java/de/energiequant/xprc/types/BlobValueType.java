package de.energiequant.xprc.types;

class BlobValueType implements ValueType<byte[]> {
    @Override
    public String serialize(byte[] value) {
        StringBuilder sb = new StringBuilder(Integer.toString(value.length));

        return sb.toString();
    }

    @Override
    public byte[] deserialize(String s) {
        return new byte[0];
    }
}
