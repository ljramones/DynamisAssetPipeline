package org.dynamisassetpipeline.core.mesh.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class DmeshReader {
    private DmeshReader() {
    }

    public static DmeshFile read(InputStream inputStream) throws IOException {
        try (DataInputStream in = new DataInputStream(inputStream)) {
            byte[] magic = in.readNBytes(DmeshWriter.MAGIC.length);
            if (!Arrays.equals(magic, DmeshWriter.MAGIC)) {
                throw new IOException("Invalid DMESH magic");
            }

            int formatVersion = in.readInt();
            int flags = in.readInt();
            int payloadLength = in.readInt();
            if (payloadLength < 0) {
                throw new IOException("Invalid payload length: " + payloadLength);
            }
            byte[] payload = in.readNBytes(payloadLength);
            if (payload.length != payloadLength) {
                throw new IOException("Unexpected EOF while reading payload");
            }

            return new DmeshFile(magic, formatVersion, flags, payloadLength, payload);
        }
    }
}
