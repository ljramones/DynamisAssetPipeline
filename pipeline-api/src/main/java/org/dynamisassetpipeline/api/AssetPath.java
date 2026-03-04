package org.dynamisassetpipeline.api;

import java.nio.file.Path;
import java.util.Objects;

public record AssetPath(Path value) {
    public AssetPath {
        Objects.requireNonNull(value, "value");
    }
}
