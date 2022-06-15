package org.cc.joe.extract;

import org.cc.joe.Mesh;
import org.cc.joe.extract.mesh.MeshExtractor;
import org.cc.joe.extract.skeleton.SkeletonExtractor;
import org.cc.joe.extract.skeleton.SkeletonNode;
import org.cc.joe.extract.texture.TextureExtractor;
import org.cc.joe.extract.xml.XmlExtractor;
import org.cc.joe.util.math.Cube;
import org.cc.joe.util.math.Dimensions;
import org.cc.joe.util.math.Point3D;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.writeString;
import static javax.imageio.ImageIO.write;

public final class ModelExtractor {

    public static void extract(final Path file) throws Exception {
        final var fileName = file.getFileName();
        final var data = ByteBuffer.wrap(readAllBytes(file));

        // extract the texture data (mip-mapped data follows, but just take the high-res data)
        final var texture = TextureExtractor.extract(data, 0x910, new Dimensions(1024, 1024));
        write(texture, "png", file.resolveSibling(fileName + ".texture.png").toFile());
        // create an accompanying .mtl file
        try (final Writer output = new BufferedWriter(new FileWriter(file.resolveSibling(fileName + ".mtl").toFile()))) {
            output.write("newmtl textured\nKa 1.000 1.000 1.000\nKd 1.000 1.000 1.000\nKs 0.000 0.000 0.000\nd 0.800000\nillum 3\nmap_Ka " + fileName + ".texture.png\nmap_Kd " + fileName + ".texture.png");
        }

        // extract the model and write to an .obj file, referencing the material data written before
        try (final Writer obj = new BufferedWriter(new FileWriter(file.resolveSibling(fileName + ".obj").toFile()))) {
            obj.write("mtllib " + fileName + ".mtl\nusemtl textured\n");
            writeMeshes(MeshExtractor.extract(data, 0x84892E), obj);
        }

        // extract the global pose of the mesh and visualize the position of each joint as a cube,
        // writing all the cubes to a single other .obj file
        try (final Writer obj = new BufferedWriter(new FileWriter(file.resolveSibling(fileName + ".skeleton.obj").toFile()))) {
            writeSkeletons(SkeletonExtractor.extract(data, 0xA4056F), obj);
        }

        // extract the various xml structures at the end of the file, containing various effects configurations
        final var xmls = XmlExtractor.extract(data, 0xC82849);
        for (final var xml : xmls) {
            writeString(file.resolveSibling(xml.name() + ".xml"), xml.content());
        }
    }

    private static void writeMeshes(final List<Mesh> meshes, final Writer output) throws IOException {
        var meshIndex = 0;
        var vertexIndexOffset = 1;
        for (; meshIndex < meshes.size(); meshIndex++) {
            final var mesh = meshes.get(meshIndex);
            writeMesh("mesh_" + meshIndex, mesh, vertexIndexOffset, output);
            vertexIndexOffset += mesh.vertexCount();
        }
    }

    private static void writeMesh(final String name, final Mesh mesh, final int vertexIndexOffset, final Writer output) throws IOException {
        output.write(format("o %s%n", name));
        for (final var vertex : mesh.vertices()) {
            output.write(format("v %f %f %f%n", vertex.x(), vertex.y(), vertex.z()));
        }
        for (final var uv : mesh.uvs()) {
            final var u = uv.u().value();
            final var v = uv.v().value();
            output.write(format("vt %f %f%n", u, v));
        }
        for (final var face : mesh.faces()) {
            final var fv1 = face.v1() + vertexIndexOffset;
            final var fv2 = face.v2() + vertexIndexOffset;
            final var fv3 = face.v3() + vertexIndexOffset;
            output.write(format("f %d/%d %d/%d %d/%d%n", fv1, fv1, fv2, fv2, fv3, fv3));
        }
    }

    private static void writeSkeletons(final List<SkeletonNode> skeletons, final Writer output) throws IOException {
        var vertexIndexOffset = 1;
        for (final SkeletonNode skeleton : skeletons) {
            vertexIndexOffset = writeSkeleton(skeleton, vertexIndexOffset, output);
        }
    }

    private static int writeSkeleton(final SkeletonNode skeleton, final int vertexIndexOffset, final Writer output) throws IOException {
        final var pose = skeleton.pose().inverse();
        writeCubeAt(skeleton.name(), new Cube(Point3D.of(pose.get(3, 0), pose.get(3, 1), pose.get(3, 2)), 1.0), vertexIndexOffset, output);
        var nextOffset = vertexIndexOffset + 8;
        for (final SkeletonNode child : skeleton.children()) {
            nextOffset = writeSkeleton(child, nextOffset, output);
        }
        return nextOffset;
    }

    private static void writeCubeAt(final String name, final Cube cube, int vertexIndexOffset, final Writer output) throws IOException {
        final var half = cube.sideLength() / 2.0;
        output.write(format("o %s%n", name));
        for (final var x : List.of(cube.center().x() - half, cube.center().x() + half)) {
            for (final var y : List.of(cube.center().y() - half, cube.center().y() + half)) {
                for (final var z : List.of(cube.center().z() - half, cube.center().z() + half)) {
                    output.write(format("v %f %f %f%n", x, y, z));
                }
            }
        }
        output.write(format("f %d %d %d %d%n", vertexIndexOffset + 0, vertexIndexOffset + 1, vertexIndexOffset + 3, vertexIndexOffset + 2));
        output.write(format("f %d %d %d %d%n", vertexIndexOffset + 4, vertexIndexOffset + 6, vertexIndexOffset + 7, vertexIndexOffset + 5));
        output.write(format("f %d %d %d %d%n", vertexIndexOffset + 0, vertexIndexOffset + 4, vertexIndexOffset + 5, vertexIndexOffset + 1));
        output.write(format("f %d %d %d %d%n", vertexIndexOffset + 2, vertexIndexOffset + 3, vertexIndexOffset + 7, vertexIndexOffset + 6));
        output.write(format("f %d %d %d %d%n", vertexIndexOffset + 0, vertexIndexOffset + 2, vertexIndexOffset + 6, vertexIndexOffset + 4));
        output.write(format("f %d %d %d %d%n", vertexIndexOffset + 1, vertexIndexOffset + 5, vertexIndexOffset + 7, vertexIndexOffset + 3));
    }
}
