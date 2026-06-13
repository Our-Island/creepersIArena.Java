package top.ourisland.creepersiarena.core.game.death;

import top.ourisland.creepersiarena.api.game.death.*;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.List;

public final class DeathResolutionRegistry implements IDeathResolutionRegistry {

    private final OwnedRegistry<DeathResolverId, IDeathCauseResolver> resolvers;
    private final OwnedRegistry<DeathMessageProviderId, IDeathMessageProvider> messageProviders;
    private final OwnedRegistry<DeathCleanupParticipantId, IDeathCleanupParticipant> cleanupParticipants;

    public DeathResolutionRegistry(@lombok.NonNull NamespaceRegistry namespaces) {
        this.resolvers = new OwnedRegistry<>(namespaces);
        this.messageProviders = new OwnedRegistry<>(namespaces);
        this.cleanupParticipants = new OwnedRegistry<>(namespaces);
    }

    @Override
    public void registerResolver(
            RegistrationOwner owner,
            DeathResolverId id,
            @lombok.NonNull IDeathCauseResolver resolver
    ) {
        resolvers.register(owner, id, resolver);
    }

    @Override
    public void registerMessageProvider(
            RegistrationOwner owner,
            DeathMessageProviderId id,
            @lombok.NonNull IDeathMessageProvider provider
    ) {
        messageProviders.register(owner, id, provider);
    }

    @Override
    public void registerCleanupParticipant(
            RegistrationOwner owner,
            DeathCleanupParticipantId id,
            @lombok.NonNull IDeathCleanupParticipant participant
    ) {
        cleanupParticipants.register(owner, id, participant);
    }

    public List<RegisteredComponent<DeathResolverId, IDeathCauseResolver>> resolvers() {
        return resolvers.entries();
    }

    public List<RegisteredComponent<DeathMessageProviderId, IDeathMessageProvider>> messageProviders() {
        return messageProviders.entries();
    }

    public List<RegisteredComponent<DeathCleanupParticipantId, IDeathCleanupParticipant>> cleanupParticipants() {
        return cleanupParticipants.entries();
    }

    public void clearOwner(@lombok.NonNull RegistrationOwner owner) {
        resolvers.clearOwner(owner);
        messageProviders.clearOwner(owner);
        cleanupParticipants.clearOwner(owner);
    }

    public void clear() {
        resolvers.clear();
        messageProviders.clear();
        cleanupParticipants.clear();
    }

}
