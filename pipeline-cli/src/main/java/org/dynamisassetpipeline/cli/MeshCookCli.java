package org.dynamisassetpipeline.cli;

import org.dynamisassetpipeline.api.CookProfile;
import org.dynamisassetpipeline.api.manifest.ManifestEntryOut;
import org.dynamisassetpipeline.api.manifest.ManifestOut;
import org.dynamisassetpipeline.api.mesh.MeshCookRequest;
import org.dynamisassetpipeline.api.mesh.MeshCookResult;
import org.dynamisassetpipeline.core.mesh.MeshCooker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MeshCookCli {
    private MeshCookCli() {
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> parsed = parseArgs(args);

        String assetId = required(parsed, "--assetId");
        Path source = Path.of(required(parsed, "--source"));
        Path outDir = Path.of(required(parsed, "--outDir"));
        Path manifestPath = Path.of(required(parsed, "--manifest"));
        CookProfile profile = parseProfile(required(parsed, "--profile"));

        MeshCooker cooker = new MeshCooker();
        MeshCookResult result = cooker.cook(new MeshCookRequest(assetId, source, profile, outDir));

        ManifestOut current = ManifestWriter.readOrDefault(manifestPath);
        ManifestEntryOut updated = new ManifestEntryOut(
                result.assetId(),
                result.typeId(),
                toUri(outDir, result.assetId()),
                List.of()
        );

        List<ManifestEntryOut> merged = new ArrayList<>();
        boolean replaced = false;
        for (ManifestEntryOut entry : current.entries()) {
            if (entry.id().equals(updated.id())) {
                merged.add(updated);
                replaced = true;
            } else {
                merged.add(entry);
            }
        }
        if (!replaced) {
            merged.add(updated);
        }
        merged.sort(Comparator.comparing(ManifestEntryOut::id));

        ManifestWriter.write(new ManifestOut(current.version(), merged), manifestPath);
    }

    private static String toUri(Path outDir, String assetId) {
        String outPrefix = outDir.getFileName() == null ? "cooked" : outDir.getFileName().toString();
        return (outPrefix + "/" + assetId + ".dmesh").replace('\\', '/');
    }

    private static CookProfile parseProfile(String profile) {
        return switch (profile.toLowerCase()) {
            case "fast" -> CookProfile.REALTIME_FAST;
            case "full" -> CookProfile.REALTIME_FULL;
            default -> throw new IllegalArgumentException("Unsupported profile: " + profile + " (use fast|full)");
        };
    }

    private static Map<String, String> parseArgs(String[] args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Arguments must be provided as --key value pairs");
        }
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            values.put(args[i], args[i + 1]);
        }
        return values;
    }

    private static String required(Map<String, String> parsed, String key) {
        String value = parsed.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required argument: " + key);
        }
        return value;
    }
}
