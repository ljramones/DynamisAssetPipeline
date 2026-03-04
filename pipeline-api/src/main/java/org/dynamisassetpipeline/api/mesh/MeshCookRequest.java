package org.dynamisassetpipeline.api.mesh;

import org.dynamisassetpipeline.api.CookProfile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public record MeshCookRequest(
        String assetId,
        Path sourceFile,
        CookProfile profile,
        Path outputDir
) {
    public MeshCookRequest {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(sourceFile, "sourceFile");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(outputDir, "outputDir");

        if (assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
        if (!Files.exists(sourceFile)) {
            throw new IllegalArgumentException("sourceFile must exist: " + sourceFile);
        }
    }
}
