package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

/**
 * Extension identity together with the namespace it exclusively owns.
 */
public record RegistrationOwner(
        @lombok.NonNull ExtensionId extensionId,
        @lombok.NonNull CiaNamespace namespace
) {

    public static final RegistrationOwner CORE = new RegistrationOwner(
            new ExtensionId("core"),
            CiaNamespace.CORE
    );

    @Override
    public @NonNull String toString() {
        return extensionId.value() + "[" + namespace.value() + "]";
    }

}
