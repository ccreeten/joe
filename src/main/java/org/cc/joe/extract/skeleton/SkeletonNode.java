package org.cc.joe.extract.skeleton;

import org.cc.joe.util.math.Matrix;

import java.util.ArrayList;
import java.util.List;

public record SkeletonNode(Joint joint, List<SkeletonNode> children) {

    SkeletonNode(final Joint joint) {
        this(joint, new ArrayList<>());
    }

    void addChild(final SkeletonNode child) {
        children.add(child);
    }

    public String name() {
        return joint.name();
    }

    public Matrix pose() {
        return joint.pose();
    }
}
