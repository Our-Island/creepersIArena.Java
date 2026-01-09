package top.ourisland.creepersiarena.game.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class SpawnpointSelector {

    public Location pickBattleLeastCrowded(ArenaInstance arena, Collection<? extends Player> candidates, double radius) {
        List<Location> sp = arena.spawnpoints();
        if (sp.isEmpty()) return arena.anchor();

        int best = Integer.MAX_VALUE;
        List<Location> bestList = new ArrayList<>();

        double r2 = radius * radius;

        for (Location base : sp) {
            int count = 0;
            for (Player p : candidates) {
                if (p == null || !p.isOnline()) continue;
                p.getWorld();
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

    public Location pickBattle(ArenaInstance arena) {
        List<Location> sp = arena.spawnpoints();
        if (sp.isEmpty()) return arena.anchor();
        return sp.get(ThreadLocalRandom.current().nextInt(sp.size())).clone();
    }

    /**
     * steal 用：arena.yml 的 spawnpoint.red / spawnpoint.blue 是“单点”
     */
    public Location pickTeam(ArenaInstance arena, String teamKey) {
        if (teamKey == null) return arena.anchor();
        Location loc = arena.teamSpawnpoints().get(teamKey);
        return loc == null ? arena.anchor() : loc.clone();
    }
}
