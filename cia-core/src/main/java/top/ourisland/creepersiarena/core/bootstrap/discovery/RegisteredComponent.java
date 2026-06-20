package top.ourisland.creepersiarena.core.bootstrap.discovery;

import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

/**
 * Registry entry carrying the exact owner and typed registry id.
 */
public record RegisteredComponent<K, T>(
        @lombok.NonNull RegistrationOwner owner,
        @lombok.NonNull K id,
        @lombok.NonNull T value
) {

}
