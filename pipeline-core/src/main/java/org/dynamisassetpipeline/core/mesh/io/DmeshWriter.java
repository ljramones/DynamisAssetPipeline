package org.dynamisassetpipeline.core.mesh.io;

import org.meshforge.pack.buffer.PackedMesh;
import org.meshforge.pack.layout.VertexLayout;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DmeshWriter {
    public static final byte[] MAGIC = new byte[]{'D', 'M', 'E', 'S', 'H', 0, 0, 0};
    public static final int FORMAT_VERSION = 1;

    private DmeshWriter() {
    }

    public static void write(PackedMesh packedMesh, OutputStream outputStream) throws IOException {
        byte[] payload = serializePackedMesh(packedMesh);
        try (DataOutputStream out = new DataOutputStream(outputStream)) {
            out.write(MAGIC);
            out.writeInt(FORMAT_VERSION);
            out.writeInt(0);
            out.writeInt(payload.length);
            out.write(payload);
        }
    }

    private static byte[] serializePackedMesh(PackedMesh packedMesh) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            VertexLayout layout = packedMesh.layout();
            out.writeInt(layout.strideBytes());

            List<VertexLayout.Entry> entries = new ArrayList<>(layout.entries().values());
            entries.sort(Comparator
                    .comparing((VertexLayout.Entry e) -> e.key().semantic().name())
                    .thenComparingInt(e -> e.key().setIndex()));

            out.writeInt(entries.size());
            for (VertexLayout.Entry entry : entries) {
                writeString(out, entry.key().semantic().name());
                out.writeInt(entry.key().setIndex());
                writeString(out, entry.format().name());
                out.writeInt(entry.offsetBytes());
            }

            byte[] vertexBytes = toArray(packedMesh.vertexBuffer());
            out.writeInt(vertexBytes.length);
            out.write(vertexBytes);

            out.writeInt(packedMesh.indexBuffer().type().ordinal());
            out.writeInt(packedMesh.indexBuffer().indexCount());
            byte[] indexBytes = toArray(packedMesh.indexBuffer().buffer());
            out.writeInt(indexBytes.length);
            out.write(indexBytes);

            List<PackedMesh.SubmeshRange> submeshes = packedMesh.submeshes();
            out.writeInt(submeshes.size());
            for (PackedMesh.SubmeshRange submesh : submeshes) {
                out.writeInt(submesh.firstIndex());
                out.writeInt(submesh.indexCount());
                Object materialId = submesh.materialId();
                writeString(out, materialId == null ? "" : materialId.toString());
            }
        }
        return baos.toByteArray();
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static byte[] toArray(ByteBuffer source) {
        ByteBuffer copy = source.duplicate();
        copy.clear();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return bytes;
    }
}
