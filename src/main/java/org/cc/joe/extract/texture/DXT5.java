package org.cc.joe.extract.texture;

import org.cc.joe.util.math.Dimensions;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.cc.joe.util.data.Conversion.u8;

// BC3 (DXT5) decompression
public final class DXT5 {

    private DXT5() {
    }

    public static BufferedImage decode(final ByteBuffer data, final Dimensions dimensions) {
        final var texture = new BufferedImage(dimensions.width(), dimensions.height(), TYPE_INT_ARGB);
        for (var y = 0; y < dimensions.height(); y += 4) {
            for (var x = 0; x < dimensions.width(); x += 4) {
                if (data.position() == data.capacity()) {
                    continue;
                }
                // read reference alphas
                final var alphas = new int[8];
                alphas[0] = data.get();
                alphas[1] = data.get();

                if (alphas[0] > alphas[1]) {
                    for (var i = 2; i < 8; i++) {
                        alphas[i] = (int) (((8 - i) / 7.0) * alphas[0] + ((i - 1) / 7.0) * alphas[1]);
                    }
                } else {
                    for (var i = 2; i < 6; i++) {
                        alphas[i] = (int) (((6 - i) / 5.0) * alphas[0] + ((i - 1) / 7.0) * alphas[1]);
                    }
                    alphas[6] = 0;
                    alphas[7] = 255;
                }
                // read alpha indices
                final var alphaIndices = new int[]{
                        u8(data.get()) | u8(data.get()) << 8 | u8(data.get()) << 16,
                        u8(data.get()) | u8(data.get()) << 8 | u8(data.get()) << 16
                };

                // RGB 5:6:5
                final var colour0 = Colour.fromRgb565(data.getShort());
                final var colour1 = Colour.fromRgb565(data.getShort());
                // linearly interpolate to get 4 colours total
                final var colour2 = lerp(colour0, colour1, 2.0 / 3.0);
                final var colour3 = lerp(colour0, colour1, 1.0 / 3.0);

                // create colour index table
                final var palette = new Colour[]{colour0, colour1, colour2, colour3};
                for (var row = 0; row < 4; row++) {
                    var indices = data.get();
                    var alphaRow = row / 2;
                    for (var col = 0; col < 4; col++) {
                        final var colour = palette[indices & 0b11];
                        final var alpha = alphas[alphaIndices[alphaRow] & 0b111];

                        texture.setRGB(x + col, dimensions.height() - (y + row) - 1, colour.withAlpha(alpha).intArgb());

                        indices >>= 2;
                        alphaIndices[alphaRow] >>= 3;
                    }
                }
            }
        }
        return texture;
    }

    private static Colour lerp(final Colour a, final Colour b, final double fraction) {
        return a.multiply(fraction).add(b.multiply(1.0 - fraction));
    }
}
