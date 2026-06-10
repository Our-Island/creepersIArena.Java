package top.ourisland.creepersiarena.core.economy.cosmetic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.economy.cosmetic.*;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.player.PlayerDataParticipant;
import top.ourisland.creepersiarena.core.player.PlayerDataService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class CosmeticService implements ICosmeticService, PlayerDataParticipant {

    private final Logger logger;
    private final JdbcDatabaseService database;
    private final CosmeticRepository repository;
    private final PlayerDataService playerData;
    private final CosmeticRegistry registry;
    private final IAbilityGate abilities;
    private final Random random = new Random();
    private final Map<UUID, Long> suppressedUntilTick = new ConcurrentHashMap<>();
    private final Map<UUID, Set<CosmeticId>> unlockedByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, Map<CosmeticSlot, CosmeticId>> selectionsByPlayer = new ConcurrentHashMap<>();
    private long currentTick;
    private double viewerRadius = 15.0D;

    public CosmeticService(
            Logger logger,
            JdbcDatabaseService database,
            PlayerDataService playerData,
            CosmeticRegistry registry,
            IAbilityGate abilities
    ) {
        this.logger = logger;
        this.database = database;
        this.repository = new CosmeticRepository(database);
        this.playerData = playerData;
        this.registry = registry;
        this.abilities = abilities;
        this.playerData.registerParticipant(this);
    }

    @Override
    public void load(UUID playerId) throws Exception {
        unlockedByPlayer.put(playerId, ConcurrentHashMap.newKeySet());
        unlockedByPlayer.get(playerId).addAll(repository.loadUnlocks(playerId));
        selectionsByPlayer.put(playerId, new ConcurrentHashMap<>(repository.loadSelections(playerId)));
    }

    @Override
    public void unload(UUID playerId) {
        unlockedByPlayer.remove(playerId);
        selectionsByPlayer.remove(playerId);
        suppressedUntilTick.remove(playerId);
    }

    @Override
    public void flushAll() throws Exception {
        for (var entry : unlockedByPlayer.entrySet()) {
            for (CosmeticId cosmeticId : entry.getValue()) {
                repository.saveUnlock(entry.getKey(), cosmeticId);
            }
        }

        for (var entry : selectionsByPlayer.entrySet()) {
            for (var selection : entry.getValue().entrySet()) {
                repository.saveSelection(entry.getKey(), selection.getKey(), selection.getValue());
            }
        }
    }

    public void tick() {
        currentTick++;
        if (!abilities.isEnabledForGame(CoreAbilities.COSMETIC_RUNTIME, "cosmetic_tick")) return;

        Bukkit.getOnlinePlayers().forEach(this::tickPlayer);
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
        return playerData.loaded(playerId)
                && unlockedByPlayer.containsKey(playerId)
                && selectionsByPlayer.containsKey(playerId);
    }

    @Override
    public boolean isUnlocked(
            UUID playerId,
            CosmeticId cosmeticId
    ) {
        if (cosmeticId == null || !loaded(playerId)) return false;
        return unlockedByPlayer.getOrDefault(playerId, Set.of()).contains(cosmeticId);
    }

    @Override
    public void unlock(
            UUID playerId,
            CosmeticId cosmeticId
    ) {
        if (cosmeticId == null || !loaded(playerId)) return;

        unlockedByPlayer.computeIfAbsent(playerId, _ -> ConcurrentHashMap.newKeySet()).add(cosmeticId);
        database.runAsync(() -> repository.saveUnlock(playerId, cosmeticId)).exceptionally(error -> {
            logger.warn("[Cosmetic] Failed to persist unlock {} for {}: {}", cosmeticId, playerId, error.getMessage(), error);
            return null;
        });
    }

    @Override
    public CosmeticId selected(
            UUID playerId,
            CosmeticSlot slot
    ) {
        if (slot == null || !loaded(playerId)) return null;
        return selectionsByPlayer.getOrDefault(playerId, Map.of()).get(slot);
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

        selectionsByPlayer.computeIfAbsent(playerId, _ -> new ConcurrentHashMap<>()).put(slot, cosmeticId);
        database.runAsync(() -> repository.saveSelection(playerId, slot, cosmeticId)).exceptionally(error -> {
            logger.warn("[Cosmetic] Failed to persist selection {} for {}: {}", cosmeticId, playerId, error.getMessage(), error);
            return null;
        });

        return true;
    }

    @Override
    public void clearSelection(
            UUID playerId,
            CosmeticSlot slot
    ) {
        if (slot == null || !loaded(playerId)) return;

        selectionsByPlayer.computeIfAbsent(playerId, _ -> new ConcurrentHashMap<>()).remove(slot);
        database.runAsync(() -> repository.saveSelection(playerId, slot, null)).exceptionally(error -> {
            logger.warn("[Cosmetic] Failed to clear selection for {} slot {}: {}", playerId, slot, error.getMessage(), error);
            return null;
        });
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

    public void viewerRadius(double viewerRadius) {
        this.viewerRadius = Math.max(0.0D, viewerRadius);
    }

}
