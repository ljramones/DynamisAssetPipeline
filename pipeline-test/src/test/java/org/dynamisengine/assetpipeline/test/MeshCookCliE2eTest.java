package org.dynamisengine.assetpipeline.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dynamisengine.assetpipeline.cli.MeshCookCli;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeshCookCliE2eTest {

    @Test
    void cliProducesDeterministicCookedMeshAndManifest() throws Exception {
        Path workspace = Files.createTempDirectory("pipeline-e2e");
        Path cookedDir = workspace.resolve("cooked");
        Path sourceFile = workspace.resolve("tiny_triangle.obj");
        Path manifest = workspace.resolve("manifest.json");

        try (InputStream in = getClass().getResourceAsStream("/fixtures/obj/tiny_triangle.obj")) {
            if (in == null) {
                throw new IllegalStateException("Missing test fixture");
            }
            Files.copy(in, sourceFile);
        }

        String[] args = {
                "--assetId", "mesh/props/crate",
                "--source", sourceFile.toString(),
                "--outDir", cookedDir.toString(),
                "--profile", "fast",
                "--manifest", manifest.toString()
        };

        MeshCookCli.main(args);

        Path cookedFile = cookedDir.resolve("mesh/props/crate.dmesh");
        assertTrue(Files.exists(cookedFile));
        assertTrue(Files.exists(manifest));

        byte[] firstCook = Files.readAllBytes(cookedFile);
        byte[] firstManifest = Files.readAllBytes(manifest);

        MeshCookCli.main(args);

        byte[] secondCook = Files.readAllBytes(cookedFile);
        byte[] secondManifest = Files.readAllBytes(manifest);

        assertArrayEquals(firstCook, secondCook);
        assertArrayEquals(firstManifest, secondManifest);

        JsonNode root = new ObjectMapper().readTree(secondManifest);
        assertEquals(1, root.get("version").asInt());
        assertEquals(1, root.get("entries").size());

        JsonNode entry = root.get("entries").get(0);
        assertEquals("mesh/props/crate", entry.get("id").asText());
        assertEquals("mesh.packed.dmesh.v0", entry.get("typeId").asText());
        assertEquals("cooked/mesh/props/crate.dmesh", entry.get("uri").asText());
        assertEquals(0, entry.get("dependencies").size());
    }
}
