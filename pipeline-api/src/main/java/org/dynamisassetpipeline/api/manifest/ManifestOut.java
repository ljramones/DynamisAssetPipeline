package org.dynamisassetpipeline.api.manifest;

import java.util.List;
import java.util.Objects;

public record ManifestOut(int version, List<ManifestEntryOut> entries) {
    public ManifestOut {
        Objects.requireNonNull(entries, "entries");
        if (version < 1) {
            throw new IllegalArgumentException("version must be >= 1");
        }
        entries = List.copyOf(entries);
    }
}
