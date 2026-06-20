package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

/**
 * Opaque registration capability for one extension namespace.
 * <p>
 * Runtime instances are issued by the core and compared by identity. Extension code can inspect the textual extension
 * id and namespace attached to its own capability, but cannot impersonate core/another extension by recreating the same
 * values. Core registries only accept runtime-issued owner instances.
 */
public interface RegistrationOwner {

    @NonNull ExtensionId extensionId();

    @NonNull CiaNamespace namespace();

    /**
     * Returns whether both references represent the exact same runtime-issued authority.
     */
    default boolean sameAuthority(RegistrationOwner other) {
        return this == other;
    }

}
