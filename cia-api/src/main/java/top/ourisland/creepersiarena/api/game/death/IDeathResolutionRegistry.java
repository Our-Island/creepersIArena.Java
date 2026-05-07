package top.ourisland.creepersiarena.api.game.death;

public interface IDeathResolutionRegistry {

    void registerResolver(String ownerId, IDeathCauseResolver resolver);

    void registerMessageProvider(String ownerId, IDeathMessageProvider provider);

    void registerCleanupParticipant(String ownerId, IDeathCleanupParticipant participant);

}
