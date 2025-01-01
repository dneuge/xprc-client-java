package de.energiequant.xprc.utils;

public class ImmutablePair<L, R> {
    private final L left;
    private final R right;

    private ImmutablePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public static <T, U> ImmutablePair<T, U> of(T left, U right) {
        return new ImmutablePair<>(left, right);
    }
}
