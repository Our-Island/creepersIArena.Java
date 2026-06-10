package top.ourisland.creepersiarena.api.game.death;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.ability.AbilityId;

public interface IDeathCleanupParticipant {

    /**
     * Optional fine-grained ability gate for this participant. The global core:death_cleanup_participants gate is still
     * checked first. Returning null means the participant is controlled only by the global cleanup-participants gate.
     */
    default @Nullable AbilityId abilityId() {
        return null;
    }

    void cleanupAfterDeath(Player player);

}
