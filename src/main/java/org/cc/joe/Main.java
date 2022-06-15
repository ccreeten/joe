package org.cc.joe;

import org.cc.joe.extract.ModelExtractor;

import java.nio.file.Paths;

public final class Main {

    public static void main(final String... args) throws Exception {
        ModelExtractor.extract(Paths.get("..."));
    }
}
