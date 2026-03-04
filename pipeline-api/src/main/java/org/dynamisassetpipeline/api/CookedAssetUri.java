package org.dynamisassetpipeline.api;

import java.util.Objects;

public record CookedAssetUri(String value) {
    public CookedAssetUri {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
