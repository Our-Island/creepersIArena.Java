package top.ourisland.creepersiarena.api.game.death;

import org.bukkit.entity.Player;

public interface IDeathCleanupParticipant {

    void cleanupAfterDeath(Player player);

}
