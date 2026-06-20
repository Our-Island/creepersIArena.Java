package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

public final class TestRegistrationOwners {

    private TestRegistrationOwners() {
    }

    public static RegistrationOwner issue(String extensionId, String namespace) {
        return new TestRegistrationOwner(
                ExtensionId.parse(extensionId),
                CiaNamespace.parse(namespace)
        );
    }

    private record TestRegistrationOwner(
            ExtensionId extensionId,
            CiaNamespace namespace
    ) implements RegistrationOwner {

        @Override
        public @NonNull String toString() {
            return extensionId.value() + "[" + namespace.value() + "]";
        }

    }

}
