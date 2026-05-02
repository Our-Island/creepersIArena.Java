package top.ourisland.creepersiarena.game.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class SpawnpointSelector {

    public Location pickLeastCrowded(
            List<Location> spawnpoints,
            Collection<? extends Player> candidates,
            double radius
    ) {
        if (spawnpoints == null || spawnpoints.isEmpty()) return null;

        int best = Integer.MAX_VALUE;
        List<Location> bestList = new ArrayList<>();

        double r2 = radius * radius;

        for (Location base : spawnpoints) {
            int count = 0;
            for (Player p : candidates) {
                if (p == null || !p.isOnline()) continue;
                if (base.getWorld() == null) continue;
                if (!p.getWorld().getUID().equals(base.getWorld().getUID())) continue;

                if (p.getLocation().distanceSquared(base) <= r2) count++;
            }

            if (count < best) {
                best = count;
                bestList.clear();
                bestList.add(base);
            } else if (count == best) {
                bestList.add(base);
            }
        }

        Location picked = bestList.get(ThreadLocalRandom.current().nextInt(bestList.size()));
        return picked.clone();
    }

    public Location pickRandom(List<Location> spawnpoints) {
        if (spawnpoints == null || spawnpoints.isEmpty()) return null;
        return spawnpoints.get(ThreadLocalRandom.current().nextInt(spawnpoints.size())).clone();
    }

    public Location pickGroupFirst(ArenaInstance arena, String group) {
        if (arena == null || group == null) return null;
        var spawns = arena.spawnGroup(group);
        if (spawns.isEmpty()) return null;
        return spawns.getFirst().clone();
    }

}
