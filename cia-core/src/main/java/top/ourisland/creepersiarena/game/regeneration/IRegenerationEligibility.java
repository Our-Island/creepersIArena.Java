package top.ourisland.creepersiarena.game.regeneration;

import org.bukkit.entity.Player;

public interface IRegenerationEligibility {

    boolean allowRegeneration(Player player);

}
