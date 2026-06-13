package top.ourisland.creepersiarena.core.identity;

import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public final class DuplicateRegistrationException extends IllegalStateException {

    public DuplicateRegistrationException(
            String id,
            RegistrationOwner existingOwner,
            RegistrationOwner newOwner
    ) {
        super("Duplicate registration for " + id
                + "; existing owner=" + existingOwner
                + ", new owner=" + newOwner);
    }

}
