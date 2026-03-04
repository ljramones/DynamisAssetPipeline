package org.dynamisassetpipeline.core.mesh;

import org.dynamisassetpipeline.api.CookProfile;
import org.dynamisassetpipeline.api.mesh.MeshCookRequest;
import org.dynamisassetpipeline.api.mesh.MeshCookResult;
import org.dynamisassetpipeline.core.mesh.io.DmeshReader;
import org.dynamisassetpipeline.core.mesh.io.DmeshWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DmeshDeterminismTest {

    @Test
    void cookingSameSourceProducesIdenticalBytesAndHash() throws Exception {
        Path source = Files.createTempFile("mesh", ".obj");
        Files.writeString(source, """
                v 0 0 0
                v 1 0 0
                v 0 1 0
                vn 0 0 1
                f 1//1 2//1 3//1
                """);

        Path out1 = Files.createTempDirectory("cook-a");
        Path out2 = Files.createTempDirectory("cook-b");

        MeshCooker cooker = new MeshCooker();

        MeshCookResult result1 = cooker.cook(new MeshCookRequest(
                "mesh/test/triangle",
                source,
                CookProfile.REALTIME_FAST,
                out1
        ));
        MeshCookResult result2 = cooker.cook(new MeshCookRequest(
                "mesh/test/triangle",
                source,
                CookProfile.REALTIME_FAST,
                out2
        ));

        byte[] bytes1 = Files.readAllBytes(result1.cookedFile());
        byte[] bytes2 = Files.readAllBytes(result2.cookedFile());

        assertArrayEquals(bytes1, bytes2);
        assertEquals(result1.contentHash64(), result2.contentHash64());

        var parsed = DmeshReader.read(new ByteArrayInputStream(bytes1));
        assertArrayEquals(DmeshWriter.MAGIC, parsed.magic());
        assertEquals(DmeshWriter.FORMAT_VERSION, parsed.formatVersion());
        assertEquals(0, parsed.flags());
        assertEquals(parsed.payloadLength(), parsed.payload().length);
    }
}
