package top.ourisland.creepersiarena.api.game.death;

import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public interface IDeathResolutionRegistry {

    void registerResolver(
            RegistrationOwner owner,
            DeathResolverId id,
            IDeathCauseResolver resolver
    );

    void registerMessageProvider(
            RegistrationOwner owner,
            DeathMessageProviderId id,
            IDeathMessageProvider provider
    );

    void registerCleanupParticipant(
            RegistrationOwner owner,
            DeathCleanupParticipantId id,
            IDeathCleanupParticipant participant
    );

}
