package top.ourisland.creepersiarena.game.death;

import top.ourisland.creepersiarena.api.game.death.DeathAttribution;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DamageAttributionStore {

    private static final long DEFAULT_VALID_TICKS = 600L;

    private final Map<UUID, DeathAttribution> attributions = new HashMap<>();
    private final long validTicks;
    private long currentTick;

    public DamageAttributionStore() {
        this(DEFAULT_VALID_TICKS);
    }

    public DamageAttributionStore(long validTicks) {
        this.validTicks = Math.max(1L, validTicks);
    }

    public long currentTick() {
        return currentTick;
    }

    public void record(
            @lombok.NonNull UUID victimId,
            @lombok.NonNull DeathAttribution attribution
    ) {
        attributions.put(victimId, attribution);
    }

    public Optional<DeathAttribution> findRecent(UUID victimId, long currentTick) {
        if (victimId == null) return Optional.empty();

        DeathAttribution attribution = attributions.get(victimId);
        if (attribution == null) return Optional.empty();

        if (isExpired(attribution, currentTick)) {
            attributions.remove(victimId);
            return Optional.empty();
        }

        return Optional.of(attribution);
    }

    private boolean isExpired(DeathAttribution attribution, long currentTick) {
        return currentTick - attribution.tick() > validTicks;
    }

    public void clear(UUID victimId) {
        if (victimId == null) return;
        attributions.remove(victimId);
    }

    public void clearAll() {
        attributions.clear();
    }

    public void tick(long currentTick) {
        this.currentTick = Math.max(this.currentTick, currentTick);
        attributions.entrySet().removeIf(
                entry -> isExpired(entry.getValue(), this.currentTick)
        );
    }

}
