package org.dynamisengine.assetpipeline.core.mesh;

import org.dynamisengine.assetpipeline.api.CookProfile;
import org.dynamisengine.assetpipeline.api.mesh.MeshCookRequest;
import org.dynamisengine.assetpipeline.api.mesh.MeshCookResult;
import org.dynamisengine.assetpipeline.core.mesh.io.DmeshWriter;
import org.dynamisengine.meshforge.api.Packers;
import org.dynamisengine.meshforge.api.Pipelines;
import org.dynamisengine.meshforge.core.mesh.MeshData;
import org.dynamisengine.meshforge.loader.MeshLoaders;
import org.dynamisengine.meshforge.pack.buffer.PackedMesh;
import org.dynamisengine.meshforge.pack.packer.MeshPacker;

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
