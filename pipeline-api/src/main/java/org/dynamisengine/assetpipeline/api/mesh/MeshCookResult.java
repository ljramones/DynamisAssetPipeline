package org.dynamisengine.assetpipeline.api.mesh;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record MeshCookResult(
        String assetId,
        String typeId,
        Path cookedFile,
        long contentHash64,
        List<String> dependencies
) {
    public MeshCookResult {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(typeId, "typeId");
        Objects.requireNonNull(cookedFile, "cookedFile");
        Objects.requireNonNull(dependencies, "dependencies");

        if (assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
        if (typeId.isBlank()) {
            throw new IllegalArgumentException("typeId must not be blank");
        }

        dependencies = List.copyOf(dependencies);
    }
}
