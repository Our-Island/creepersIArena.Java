package top.ourisland.creepersiarena.api.extension;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.List;

/**
 * Parsed metadata from a CIA extension jar's {@code cia-extension.yml} file.
 */
public record CiaExtensionDescriptor(
        @lombok.NonNull ExtensionId id,
        @lombok.NonNull CiaNamespace namespace,
        @lombok.NonNull String name,
        @lombok.NonNull String version,
        @lombok.NonNull String mainClass,
        int apiVersion,
        @lombok.NonNull String ciaVersion,
        @lombok.NonNull List<String> authors,
        @lombok.NonNull List<CiaExtensionDependency> dependencies,
        @lombok.NonNull CiaExtensionLoadOrder loadOrder
) {

    public static final String DESCRIPTOR_ENTRY = "cia-extension.yml";
    public static final int CURRENT_API_VERSION = 2;

    public @NonNull RegistrationOwner owner() {
        return new RegistrationOwner(id, namespace);
    }

    public @NonNull List<ExtensionId> requiredDependencyIds() {
        return dependencies.stream()
                .filter(dependency -> !dependency.optional())
                .map(CiaExtensionDependency::id)
                .toList();
    }

    public @NonNull List<ExtensionId> optionalDependencyIds() {
        return dependencies.stream()
                .filter(CiaExtensionDependency::optional)
                .map(CiaExtensionDependency::id)
                .toList();
    }

}
