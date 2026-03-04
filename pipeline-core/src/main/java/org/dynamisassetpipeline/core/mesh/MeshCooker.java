package org.dynamisassetpipeline.core.mesh;

import org.dynamisassetpipeline.api.CookProfile;
import org.dynamisassetpipeline.api.mesh.MeshCookRequest;
import org.dynamisassetpipeline.api.mesh.MeshCookResult;
import org.dynamisassetpipeline.core.mesh.io.DmeshWriter;
import org.meshforge.api.Packers;
import org.meshforge.api.Pipelines;
import org.meshforge.core.mesh.MeshData;
import org.meshforge.loader.MeshLoaders;
import org.meshforge.pack.buffer.PackedMesh;
import org.meshforge.pack.packer.MeshPacker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public final class MeshCooker {
    public static final String TYPE_ID = "mesh.packed.dmesh.v0";

    public MeshCookResult cook(MeshCookRequest request) throws IOException {
        MeshData loaded = MeshLoaders.defaults().load(request.sourceFile());
        MeshData processed = switch (request.profile()) {
            case REALTIME_FAST -> Pipelines.realtimeFast(loaded);
            case REALTIME_FULL -> Pipelines.realtime(loaded);
        };

        PackedMesh packed = switch (request.profile()) {
            case REALTIME_FAST -> MeshPacker.pack(processed, Packers.realtimeFast());
            case REALTIME_FULL -> MeshPacker.pack(processed, Packers.realtime());
        };

        Path outputPath = outputPathFor(request.outputDir(), request.assetId());
        Files.createDirectories(outputPath.getParent());
        try (var out = Files.newOutputStream(outputPath)) {
            DmeshWriter.write(packed, out);
        }

        byte[] cookedBytes = Files.readAllBytes(outputPath);
        return new MeshCookResult(
                request.assetId(),
                TYPE_ID,
                outputPath,
                truncatedSha256ToLong(cookedBytes),
                List.of()
        );
    }

    private static Path outputPathFor(Path outputDir, String assetId) {
        return outputDir.resolve(assetId + ".dmesh");
    }

    private static long truncatedSha256ToLong(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            long value = 0L;
            for (int i = 0; i < Long.BYTES; i++) {
                value = (value << 8) | (hash[i] & 0xffL);
            }
            return value;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
