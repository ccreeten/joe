package org.cc.joe.util.data;

import static java.lang.Math.pow;

public final class Conversion {

    private Conversion() {
    }

    public static int u8(final byte value) {
        return value & 0xFF;
    }

    public static float f16(final int bits) {
        final var sign = Bits.get(bits, 15);
        final var exponent = Bits.get(bits, 14, 10);
        final var fraction = Bits.get(bits, 0, 9);
        return (float) (exponent == 0
                ? (pow(-1, sign) * pow(2, -14) * (0 + fraction / 1024.0D))
                : (pow(-1, sign) * pow(2, exponent - 15) * (1 + fraction / 1024.0D))
        );
    }
}
