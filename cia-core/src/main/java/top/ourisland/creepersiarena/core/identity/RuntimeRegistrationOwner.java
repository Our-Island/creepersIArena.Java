package top.ourisland.creepersiarena.core.identity;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

record RuntimeRegistrationOwner(
        ExtensionId extensionId,
        CiaNamespace namespace
) implements RegistrationOwner {

    RuntimeRegistrationOwner(
            @lombok.NonNull ExtensionId extensionId,
            @lombok.NonNull CiaNamespace namespace
    ) {
        this.extensionId = extensionId;
        this.namespace = namespace;
    }

    @Override
    public @NonNull String toString() {
        return extensionId.value() + "[" + namespace.value() + "]";
    }

}
