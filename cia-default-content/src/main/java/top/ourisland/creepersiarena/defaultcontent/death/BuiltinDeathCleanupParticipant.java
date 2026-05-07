package top.ourisland.creepersiarena.defaultcontent.death;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.death.IDeathCleanupParticipant;

public final class BuiltinDeathCleanupParticipant implements IDeathCleanupParticipant {

    @Override
    public void cleanupAfterDeath(Player player) {
        BuiltinDamageAttributionMarker.clearNextDamage(player);
    }

}
