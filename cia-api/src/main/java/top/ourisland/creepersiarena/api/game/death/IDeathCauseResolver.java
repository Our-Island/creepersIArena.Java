package top.ourisland.creepersiarena.api.game.death;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public interface IDeathCauseResolver {

    Optional<DeathAttribution> resolveDamage(
            EntityDamageEvent event,
            Player victim
    );

    Optional<DeathCauseId> resolveDeath(
            PlayerDeathEvent event,
            Player victim,
            @Nullable DeathAttribution lastAttribution
    );

}
