package de.energiequant.xprc.types;

class IntegerArrayValueType implements ValueType<int[]> {

    @Override
    public String serialize(int[] value) {
        StringBuilder sb = new StringBuilder(Integer.toString(value.length));

        for (int v : value) {
            sb.append(",");
            sb.append(v);
        }

        return sb.toString();
    }

    @Override
    public int[] deserialize(String s) {
        String[] split = s.split(",");
        if (split.length < 1) {
            throw new IllegalArgumentException("Bad int[] format: \"" + s + "\"");
        }

        int length = Integer.parseUnsignedInt(split[0]);
        if (length == 0) {
            throw new IllegalArgumentException("Empty int[] not supported: \"" + s + "\"");
        }

        if (length != split.length - 1) {
            throw new IllegalArgumentException("Length mismatch on int[]: \"" + s + "\"");
        }

        int[] out = new int[length];
        for (int i = 0; i < length; i++) {
            out[i] = Integer.parseInt(split[i + 1]);
        }

        return out;
    }
}
