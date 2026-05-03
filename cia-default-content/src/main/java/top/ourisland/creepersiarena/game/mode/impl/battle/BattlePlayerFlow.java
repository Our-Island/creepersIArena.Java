package top.ourisland.creepersiarena.game.mode.impl.battle;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;
import top.ourisland.creepersiarena.defaultcontent.DefaultLoadoutService;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Player flow for the bundled battle content.
 * <p>
 * Core delegates entry/respawn details here instead of hard-coding battle spawn and kit behaviour.
 */
public final class BattlePlayerFlow implements IModePlayerFlow {

    private final BattleState state;
    private final BattleTeamBalancer teams;
    private final DefaultLoadoutService kit;

    public BattlePlayerFlow(GameRuntime runtime, BattleState state, BattleTeamBalancer teams) {
        this.state = state;
        this.teams = teams;
        this.kit = new DefaultLoadoutService(
                runtime.requireService(JobManager.class),
                runtime.requireService(SkillRegistry.class),
                runtime.requireService(SkillHotbarRenderer.class),
                runtime.requireService(SkillTickTask.class)::nowTick
        );
    }

    @Override
    public Location spawnLocation(ModePlayerContext ctx) {
        ArenaInstance arena = ctx.game().arena();
        var playerSession = ctx.session();
        if (playerSession != null) {
            teams.assign(playerSession, state.players());
        }
        int team = playerSession == null || playerSession.selectedTeam() == null ? 0 : playerSession.selectedTeam();
        List<Location> spawns = team <= 0 ? List.of() : arena.spawnGroup(String.valueOf(team));
        if (spawns.isEmpty()) spawns = arena.spawnGroup("team-" + team);
        if (spawns.isEmpty()) spawns = arena.spawnGroup("default");
        if (spawns.isEmpty()) return arena.anchor();
        return pickLeastCrowded(spawns, Bukkit.getOnlinePlayers(), 10.0);
    }

    @Override
    public boolean allowHubEntrance(ModeLobbyContext ctx) {
        return state.config().entranceEnabled();
    }

    @Override
    public int selectableTeamCount(ModeLobbyContext ctx) {
        return state.config().maxTeam();
    }

    @Override
    public void onEnterGame(ModePlayerContext ctx) {
        Player player = ctx.player();
        var playerSession = ctx.session();

        state.markFighter(playerSession);

        player.setGameMode(GameMode.ADVENTURE);
        kit.apply(player, playerSession);
        Msg.actionBar(player, Component.text("进入 battle 战场"));
    }

    private Location pickLeastCrowded(
            List<Location> spawns,
            Collection<? extends Player> candidates,
            double radius
    ) {
        if (spawns.isEmpty()) return null;

        int best = Integer.MAX_VALUE;
        List<Location> bestList = new ArrayList<>();
        double r2 = radius * radius;

        for (Location base : spawns) {
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

}
