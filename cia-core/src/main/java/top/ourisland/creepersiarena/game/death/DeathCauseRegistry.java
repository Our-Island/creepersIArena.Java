package top.ourisland.creepersiarena.game.death;

import top.ourisland.creepersiarena.api.game.death.IDeathCauseResolver;
import top.ourisland.creepersiarena.api.game.death.IDeathCleanupParticipant;
import top.ourisland.creepersiarena.api.game.death.IDeathMessageProvider;
import top.ourisland.creepersiarena.api.game.death.IDeathResolutionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DeathCauseRegistry implements IDeathResolutionRegistry {

    private final List<RegisteredDeathComponent<IDeathCauseResolver>> resolvers = new ArrayList<>();
    private final List<RegisteredDeathComponent<IDeathMessageProvider>> messageProviders = new ArrayList<>();
    private final List<RegisteredDeathComponent<IDeathCleanupParticipant>> cleanupParticipants = new ArrayList<>();

    @Override
    public void registerResolver(String ownerId, IDeathCauseResolver resolver) {
        resolvers.add(new RegisteredDeathComponent<>(
                normalizeOwner(ownerId),
                Objects.requireNonNull(resolver, "resolver")
        ));
    }

    @Override
    public void registerMessageProvider(String ownerId, IDeathMessageProvider provider) {
        messageProviders.add(new RegisteredDeathComponent<>(
                normalizeOwner(ownerId),
                Objects.requireNonNull(provider, "provider")
        ));
    }

    @Override
    public void registerCleanupParticipant(String ownerId, IDeathCleanupParticipant participant) {
        cleanupParticipants.add(new RegisteredDeathComponent<>(
                normalizeOwner(ownerId),
                Objects.requireNonNull(participant, "participant")
        ));
    }

    private String normalizeOwner(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) return "unknown";
        return ownerId.trim();
    }

    public List<RegisteredDeathComponent<IDeathCauseResolver>> resolvers() {
        return List.copyOf(resolvers);
    }

    public List<RegisteredDeathComponent<IDeathMessageProvider>> messageProviders() {
        return List.copyOf(messageProviders);
    }

    public List<RegisteredDeathComponent<IDeathCleanupParticipant>> cleanupParticipants() {
        return List.copyOf(cleanupParticipants);
    }

    public void clear() {
        resolvers.clear();
        messageProviders.clear();
        cleanupParticipants.clear();
    }

    public record RegisteredDeathComponent<T>(
            String ownerId,
            T value
    ) {

    }

}
