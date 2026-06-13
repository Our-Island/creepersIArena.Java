package top.ourisland.creepersiarena.api.game.death;

import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public interface IDeathResolutionRegistry {

    void registerResolver(RegistrationOwner owner, IDeathCauseResolver resolver);

    void registerMessageProvider(RegistrationOwner owner, IDeathMessageProvider provider);

    void registerCleanupParticipant(RegistrationOwner owner, IDeathCleanupParticipant participant);

}
