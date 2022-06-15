package org.cc.joe.util.math;

public record Dimensions(int width, int height) {

    public int size() {
        return width * height;
    }
}
