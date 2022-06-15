package org.cc.joe.extract.skeleton;

import org.cc.joe.util.math.Matrix;

public final record Joint(int parent, String name, Matrix pose) {
}
