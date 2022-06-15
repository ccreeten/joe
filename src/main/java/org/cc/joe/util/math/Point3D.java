package org.cc.joe.util.math;

public record Point3D(double x, double y, double z) {

    public static Point3D of(final double x, final double y, final double z) {
        return new Point3D(x, y, z);
    }
}
