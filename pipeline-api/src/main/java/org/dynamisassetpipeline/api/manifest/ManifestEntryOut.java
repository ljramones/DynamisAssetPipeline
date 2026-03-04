package org.dynamisassetpipeline.api.manifest;

import java.util.List;
import java.util.Objects;

public record ManifestEntryOut(
        String id,
        String typeId,
        String uri,
        List<String> dependencies
) {
    public ManifestEntryOut {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(typeId, "typeId");
        Objects.requireNonNull(uri, "uri");
        Objects.requireNonNull(dependencies, "dependencies");

        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (typeId.isBlank()) {
            throw new IllegalArgumentException("typeId must not be blank");
        }
        if (uri.isBlank()) {
            throw new IllegalArgumentException("uri must not be blank");
        }

        dependencies = List.copyOf(dependencies);
    }
}
