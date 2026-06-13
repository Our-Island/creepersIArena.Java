package top.ourisland.creepersiarena.core.game.death;

import top.ourisland.creepersiarena.api.game.death.IDeathCauseResolver;
import top.ourisland.creepersiarena.api.game.death.IDeathCleanupParticipant;
import top.ourisland.creepersiarena.api.game.death.IDeathMessageProvider;
import top.ourisland.creepersiarena.api.game.death.IDeathResolutionRegistry;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.ArrayList;
import java.util.List;

public final class DeathResolutionRegistry implements IDeathResolutionRegistry {

    private final List<RegisteredDeathComponent<IDeathCauseResolver>> resolvers = new ArrayList<>();
    private final List<RegisteredDeathComponent<IDeathMessageProvider>> messageProviders = new ArrayList<>();
    private final List<RegisteredDeathComponent<IDeathCleanupParticipant>> cleanupParticipants = new ArrayList<>();

    @Override
    public synchronized void registerResolver(
            RegistrationOwner owner,
            @lombok.NonNull IDeathCauseResolver resolver
    ) {
        resolvers.add(new RegisteredDeathComponent<>(owner, resolver));
    }

    @Override
    public synchronized void registerMessageProvider(
            RegistrationOwner owner,
            @lombok.NonNull IDeathMessageProvider provider
    ) {
        messageProviders.add(new RegisteredDeathComponent<>(owner, provider));
    }

    @Override
    public synchronized void registerCleanupParticipant(
            RegistrationOwner owner,
            @lombok.NonNull IDeathCleanupParticipant participant
    ) {
        cleanupParticipants.add(new RegisteredDeathComponent<>(owner, participant));
    }

    public synchronized List<RegisteredDeathComponent<IDeathCauseResolver>> resolvers() {
        return List.copyOf(resolvers);
    }

    public synchronized List<RegisteredDeathComponent<IDeathMessageProvider>> messageProviders() {
        return List.copyOf(messageProviders);
    }

    public synchronized List<RegisteredDeathComponent<IDeathCleanupParticipant>> cleanupParticipants() {
        return List.copyOf(cleanupParticipants);
    }

    public synchronized void clearOwner(@lombok.NonNull RegistrationOwner owner) {
        resolvers.removeIf(component -> component.owner().equals(owner));
        messageProviders.removeIf(component -> component.owner().equals(owner));
        cleanupParticipants.removeIf(component -> component.owner().equals(owner));
    }

    public synchronized void clear() {
        resolvers.clear();
        messageProviders.clear();
        cleanupParticipants.clear();
    }

    public record RegisteredDeathComponent<T>(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull T value
    ) {

    }

}
