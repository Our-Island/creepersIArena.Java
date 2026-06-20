package top.ourisland.creepersiarena.api.extension;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.ExtensionId;

/**
 * Dependency declaration from {@code cia-extension.yml}.
 */
public record CiaExtensionDependency(
        @lombok.NonNull ExtensionId id,
        boolean optional
) {

    public static @NonNull CiaExtensionDependency required(ExtensionId id) {
        return new CiaExtensionDependency(id, false);
    }

    public static @NonNull CiaExtensionDependency optional(ExtensionId id) {
        return new CiaExtensionDependency(id, true);
    }

}
