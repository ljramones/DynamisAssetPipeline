package org.dynamisengine.assetpipeline.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ApiIsolationGuardTest {

    @Test
    void apiDoesNotReferenceEntityIdOrVectrixTypes() throws IOException {
        Path sourceRoot = Path.of("src/main/java");
        List<Path> files = Files.walk(sourceRoot)
                .filter(path -> path.toString().endsWith(".java"))
                .toList();

        for (Path file : files) {
            String content = Files.readString(file);
            assertFalse(content.contains("EntityId"), "Found EntityId usage in " + file);
            assertFalse(content.contains("org.vectrix"), "Found vectrix package usage in " + file);
        }
    }
}
