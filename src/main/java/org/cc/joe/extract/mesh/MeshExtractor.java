package org.cc.joe.extract.mesh;

import io.kaitai.struct.ByteBufferKaitaiStream;
import org.cc.joe.Mesh;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class MeshExtractor {

    private MeshExtractor() {
    }

    public static List<Mesh> extract(final ByteBuffer data, final int offset) {
        final var meshes = new ArrayList<Mesh>();
        final var buffer = new byte[0x100000];
        var found = seekToNextSubMesh(data.position(offset));
        while (found) {
            data.get(data.position(), buffer);
            meshes.add(new Mesh(new ByteBufferKaitaiStream(buffer)));
            found = seekToNextSubMesh(data.position(data.position() + 0x100));
        }
        return meshes;
    }

    private static boolean seekToNextSubMesh(final ByteBuffer data) {
        final var start = data.position();
        while (true) {
            final var position = data.position();
            final var vertexCount = data.getShort();
            final var faceCount = data.getShort();
            // 'heuristics'
            if (vertexCount > 0 && faceCount > 0) {
                if (data.position(data.position() + 12).getInt() == 0xFFFFFFFF && data.position(data.position() + 12).getInt() == 0xFFFFFFFF) {
                    data.position(data.position() + 12);
                    if (data.getInt() == 0xFFFFFFFF && data.get(data.position() + 12) != 0xFFFFFFFF) {
                        data.position(data.position() - 0x38);
                        return true;
                    }
                }
            }
            if (data.position() - start > 0x10000) {
                return false;
            }
            data.position(position + 1);
        }
    }
}
