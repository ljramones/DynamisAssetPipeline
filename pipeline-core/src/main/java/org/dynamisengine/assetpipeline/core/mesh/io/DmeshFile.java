package org.dynamisengine.assetpipeline.core.mesh.io;

public record DmeshFile(byte[] magic, int formatVersion, int flags, int payloadLength, byte[] payload) {
}
