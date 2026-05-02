package top.ourisland.creepersiarena.core.extension.loading;

import java.nio.file.Path;
import java.time.Instant;

public record CiaExtensionLoadFailure(
        String id,
        Path jarPath,
        String message,
        Instant failedAt
) {

    public CiaExtensionLoadFailure(
            String id,
            Path jarPath,
            String message
    ) {
        this(id, jarPath, message, Instant.now());
    }

}
