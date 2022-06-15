package org.cc.joe.util.data;

public final class Bits {

    private Bits() {
    }

    // both inclusive
    public static int get(final int bits, final int from, final int to) {
        final int count = to - from + 1;
        final int mask = (1 << count) - 1;
        return (bits >>> from) & mask;
    }

    public static int get(final int bits, final int index) {
        return get(bits, index, index);
    }

    public static boolean isSet(final int bits, final int index) {
        return (bits & (1 << index)) != 0;
    }
}
