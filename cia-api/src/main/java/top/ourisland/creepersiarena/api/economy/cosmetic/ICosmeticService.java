package top.ourisland.creepersiarena.api.economy.cosmetic;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface ICosmeticService {

    boolean loaded(UUID playerId);

    boolean isUnlocked(
            UUID playerId,
            CosmeticId cosmeticId
    );

    void unlock(
            UUID playerId,
            CosmeticId cosmeticId
    );

    @Nullable CosmeticId selected(
            UUID playerId,
            CosmeticSlot slot
    );

    boolean select(
            UUID playerId,
            CosmeticSlot slot,
            CosmeticId cosmeticId
    );

    void clearSelection(
            UUID playerId,
            CosmeticSlot slot
    );

    void suppress(
            Player player,
            String reason,
            long ticks
    );

    boolean isSuppressed(Player player);

}
