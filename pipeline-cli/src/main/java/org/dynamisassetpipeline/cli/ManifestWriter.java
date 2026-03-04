package org.dynamisassetpipeline.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.dynamisassetpipeline.api.manifest.ManifestOut;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ManifestWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    private ManifestWriter() {
    }

    public static ManifestOut readOrDefault(Path manifestPath) throws IOException {
        if (!Files.exists(manifestPath)) {
            return new ManifestOut(1, java.util.List.of());
        }
        return MAPPER.readValue(manifestPath.toFile(), ManifestOut.class);
    }

    public static void write(ManifestOut manifestOut, Path manifestPath) throws IOException {
        Path parent = manifestPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(manifestOut) + "\n";
        Files.writeString(manifestPath, json);
    }
}
