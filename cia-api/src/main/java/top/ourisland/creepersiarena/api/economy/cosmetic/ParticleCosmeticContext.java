package top.ourisland.creepersiarena.api.economy.cosmetic;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public record ParticleCosmeticContext(
        Player player,
        Location origin,
        Collection<Player> viewers,
        Random random,
        long currentTick
) {

    public ParticleCosmeticContext {
        viewers = viewers == null ? List.of() : List.copyOf(viewers);
        random = random == null ? new Random() : random;
    }

}
