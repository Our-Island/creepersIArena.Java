package top.ourisland.creepersiarena.core.extension.loading;

import top.ourisland.creepersiarena.api.identity.ExtensionId;

import java.nio.file.Path;
import java.time.Instant;

public record CiaExtensionLoadFailure(
        ExtensionId id,
        Path jarPath,
        String message,
        Instant failedAt
) {

    public CiaExtensionLoadFailure(
            ExtensionId id,
            Path jarPath,
            String message
    ) {
        this(id, jarPath, message, Instant.now());
    }

}
