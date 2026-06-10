package top.ourisland.creepersiarena.api.game.rest;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Public rest-state surface used by gameplay systems that should interrupt resting without depending on the concrete
 * resting-regeneration implementation.
 */
public interface IRestStateService {

    void breakRest(
            @Nullable Player player,
            @Nullable String reason
    );

    void clearRest(@Nullable Player player);

    void clearAllRest();

}
