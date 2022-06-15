package org.cc.joe.extract.texture;

import org.cc.joe.util.math.Dimensions;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public final class TextureExtractor {

    private TextureExtractor() {
    }

    public static BufferedImage extract(final ByteBuffer data, final int offset, final Dimensions dimensions) {
        final var bytes = new byte[dimensions.size()];
        data.position(offset);
        data.get(bytes);

        final var textureData = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
        return DXT5.decode(textureData, dimensions);
    }
}
