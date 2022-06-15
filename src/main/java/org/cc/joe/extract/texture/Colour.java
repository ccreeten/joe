package org.cc.joe.extract.texture;

public record Colour(double r, double g, double b, double a) {

    public static Colour fromRgb565(final int rgb) {
        final var r = ((rgb >>> 11) & 0b00011111) << 3;
        final var g = ((rgb >>> 5 ) & 0b00111111) << 2;
        final var b = ((rgb >>> 0 ) & 0b00011111) << 3;
        return new Colour(r / 255.0, g / 255.0, b / 255.0, 1.0);
    }

    public Colour withAlpha(final int alpha) {
        return new Colour(r, g, b, alpha / 255.0);
    }

    public int intArgb() {
        return    ((int) (a * 255.0) << 24)
                | ((int) (r * 255.0) << 16)
                | ((int) (g * 255.0) << 8 )
                | ((int) (b * 255.0) << 0 );
    }

    public Colour add(final Colour other) {
        return new Colour(r + other.r, g + other.g, b + other.b, a + other.a);
    }

    public Colour multiply(final double value) {
        return new Colour(r * value, g * value, b * value, a * value);
    }
}
