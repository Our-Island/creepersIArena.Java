package top.ourisland.creepersiarena.core.identity;

import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public final class DuplicateRegistrationException extends IllegalStateException {

    public DuplicateRegistrationException(
            String id,
            RegistrationOwner existingOwner,
            Object existingProvider,
            RegistrationOwner newOwner,
            Object newProvider
    ) {
        super("Duplicate registration for " + id
                + "; existing owner=" + existingOwner
                + provider(existingProvider)
                + ", new owner=" + newOwner
                + provider(newProvider));
    }

    private static String provider(Object value) {
        if (value == null) return "";
        var type = value.getClass();
        var name = type.getSimpleName();
        if (name.isBlank()) name = type.getName();
        return " [provider=" + name + "]";
    }

}
