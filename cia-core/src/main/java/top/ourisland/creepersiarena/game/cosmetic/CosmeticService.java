package top.ourisland.creepersiarena.game.cosmetic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.cosmetic.*;
import top.ourisland.creepersiarena.game.playerdata.PlayerDataDocument;
import top.ourisland.creepersiarena.game.playerdata.PlayerDataService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class CosmeticService implements ICosmeticService {

    private final PlayerDataService playerData;
    private final CosmeticRegistry registry;
    private final IAbilityGate abilities;
    private final Random random = new Random();
    private final Map<UUID, Long> suppressedUntilTick = new ConcurrentHashMap<>();
    private long currentTick;
    private double viewerRadius = 15.0D;

    public CosmeticService(
            PlayerDataService playerData,
            CosmeticRegistry registry,
            IAbilityGate abilities
    ) {
        this.playerData = playerData;
        this.registry = registry;
        this.abilities = abilities;
    }

    public void tick() {
        currentTick++;
        if (!abilities.isEnabledForGame(CoreAbilities.COSMETIC_RUNTIME, "cosmetic_tick")) return;

        for (var player : Bukkit.getOnlinePlayers()) {
            tickPlayer(player);
        }
    }

    private void tickPlayer(Player player) {
        if (player == null || !player.isOnline() || isSuppressed(player)) return;
        var selected = selected(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL);
        if (selected == null) return;
        var cosmetic = registry.cosmetic(selected);
        if (!(cosmetic instanceof IParticleCosmetic particleCosmetic)) return;

        int interval = particleCosmetic.schedule().intervalTicks();
        if (currentTick % interval != 0L) return;

        var origin = player.getLocation();
        var context = new ParticleCosmeticContext(
                player,
                origin,
                viewers(origin),
                random,
                currentTick
        );
        particleCosmetic.spawn(context);
    }

    private Collection<Player> viewers(Location origin) {
        if (origin == null || origin.getWorld() == null) return List.of();
        double radiusSquared = viewerRadius * viewerRadius;
        return Bukkit.getOnlinePlayers().stream()
                .filter(viewer -> viewer != null && viewer.isOnline())
                .filter(viewer -> viewer.getWorld().equals(origin.getWorld()))
                .filter(viewer -> !(viewer.getLocation().distanceSquared(origin) > radiusSquared))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean loaded(UUID playerId) {
        return playerData.loaded(playerId);
    }

    @Override
    public boolean isUnlocked(
            UUID playerId,
            CosmeticId cosmeticId
    ) {
        if (cosmeticId == null || !loaded(playerId)) return false;
        return document(playerId).getBoolean(unlockedPath(cosmeticId), false);
    }

    @Override
    public void unlock(
            UUID playerId,
            CosmeticId cosmeticId
    ) {
        if (cosmeticId == null || !loaded(playerId)) return;
        document(playerId).setBoolean(unlockedPath(cosmeticId), true);
    }

    @Override
    public CosmeticId selected(
            UUID playerId,
            CosmeticSlot slot
    ) {
        if (slot == null || !loaded(playerId)) return null;
        String raw = document(playerId).getString(selectedPath(slot), null);
        return raw == null || raw.isBlank() ? null : CosmeticId.of(raw);
    }

    @Override
    public boolean select(
            UUID playerId,
            CosmeticSlot slot,
            CosmeticId cosmeticId
    ) {
        if (slot == null || cosmeticId == null || !loaded(playerId)) return false;
        var cosmetic = registry.cosmetic(cosmeticId);
        if (cosmetic == null || cosmetic.slot() != slot) return false;
        if (!isUnlocked(playerId, cosmeticId)) return false;
        document(playerId).setString(selectedPath(slot), cosmeticId.asString());
        return true;
    }

    @Override
    public void clearSelection(
            UUID playerId,
            CosmeticSlot slot
    ) {
        if (slot == null || !loaded(playerId)) return;
        document(playerId).remove(selectedPath(slot));
    }

    @Override
    public void suppress(
            Player player,
            String reason,
            long ticks
    ) {
        if (player == null || ticks <= 0L) return;
        suppressedUntilTick.put(player.getUniqueId(), currentTick + ticks);
    }

    @Override
    public boolean isSuppressed(Player player) {
        if (player == null) return true;
        Long until = suppressedUntilTick.get(player.getUniqueId());
        return until != null && until > currentTick;
    }

    private String unlockedPath(CosmeticId id) {
        return "cosmetics.unlocked." + id.configNamespace() + "." + id.configValue();
    }

    private PlayerDataDocument document(UUID playerId) {
        return playerData.document(playerId);
    }

    private String selectedPath(CosmeticSlot slot) {
        return "cosmetics.selected." + slot.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public void viewerRadius(double viewerRadius) {
        this.viewerRadius = Math.max(0.0D, viewerRadius);
    }

}
