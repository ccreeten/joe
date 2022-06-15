package org.cc.joe.extract.skeleton;

import org.cc.joe.util.math.Matrix;

import java.nio.ByteBuffer;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.stream;

public final class SkeletonExtractor {

    private SkeletonExtractor() {
    }

    public static List<SkeletonNode> extract(final ByteBuffer input, final int position) {
        input.position(position);

        final var jointCount = input.getInt();
        final var nodes = new SkeletonNode[jointCount];

        for (var index = 0; index < jointCount; index++) {
            final var nameSize = input.getInt();
            final var nameData = new byte[nameSize];
            input.get(nameData);

            final var unknown = input.get();
            final var pose = new Matrix(new double[][]{
                    {input.getFloat(), input.getFloat(), input.getFloat(), input.getFloat()},
                    {input.getFloat(), input.getFloat(), input.getFloat(), input.getFloat()},
                    {input.getFloat(), input.getFloat(), input.getFloat(), input.getFloat()},
                    {input.getFloat(), input.getFloat(), input.getFloat(), input.getFloat()}
            });

            final var parent = input.getInt();
            for (var i = 0; i < 3; i++) {
                final var skip = input.get();
            }

            final var name = new String(nameData, US_ASCII);
            final var node = new SkeletonNode(new Joint(parent, name, pose));
            nodes[index] = node;
            if (parent != -1) {
                nodes[parent].addChild(node);
            }
        }
        return stream(nodes).filter(node -> node.joint().parent() == -1).toList();
    }
}
