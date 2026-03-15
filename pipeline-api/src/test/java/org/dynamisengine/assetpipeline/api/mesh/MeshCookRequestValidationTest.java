package org.dynamisengine.assetpipeline.api.mesh;

import org.dynamisengine.assetpipeline.api.CookProfile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MeshCookRequestValidationTest {

    @Test
    void blankAssetIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new MeshCookRequest("  ", Path.of("source.obj"), CookProfile.REALTIME_FAST, Path.of("out")));
    }

    @Test
    void missingSourceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new MeshCookRequest("mesh/props/crate", Path.of("missing.obj"), CookProfile.REALTIME_FAST, Path.of("out")));
    }
}
