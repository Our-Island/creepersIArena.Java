package top.ourisland.creepersiarena.defaultcontent.game.mode.battle;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.core.job.JobManager;
import top.ourisland.creepersiarena.core.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.core.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.core.job.skill.ui.SkillHotbarRenderer;
import top.ourisland.creepersiarena.core.utils.AttributeUtils;
import top.ourisland.creepersiarena.core.utils.Msg;
import top.ourisland.creepersiarena.defaultcontent.DefaultLoadoutService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Player flow for the bundled battle content.
 * <p>
 * Core delegates entry/respawn details here instead of hard-coding battle spawn and kit behaviour.
 */
public final class BattlePlayerFlow implements IModePlayerFlow {

    private final BattleState state;
    private final BattleTeamBalancer teams;
    private final DefaultLoadoutService kit;

    public BattlePlayerFlow(
            GameRuntime runtime,
            BattleState state,
            BattleTeamBalancer teams
    ) {
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
        var arena = ctx.game().arena();
        var playerSession = ctx.session();
        if (playerSession != null) {
            teams.assign(playerSession, state.players());
        }

        int team = playerSession == null || playerSession.selectedTeam() == null
                ? 0
                : playerSession.selectedTeam().number().orElse(0);
        List<Location> spawns = team <= 0 ? List.of() : arena.spawnGroup("team-" + team);
        if (spawns.isEmpty()) spawns = arena.spawnGroup("default");
        if (spawns.isEmpty()) return arena.anchor();

        return pickLeastCrowded(spawns, Bukkit.getOnlinePlayers(), 10.0);
    }

    @Override
    public boolean allowHubEntrance(ModeLobbyContext ctx) {
        return state.config().entranceEnabled();
    }

    @Override
    public List<TeamId> selectableTeams(ModeLobbyContext ctx) {
        return IntStream.rangeClosed(1, state.config().maxTeam())
                .mapToObj(TeamId::numbered)
                .toList();
    }

    @Override
    public void onEnterGame(ModePlayerContext ctx) {
        var player = ctx.player();
        var playerSession = ctx.session();

        state.markFighter(playerSession);

        player.setGameMode(GameMode.ADVENTURE);
        AttributeUtils.setBaseValue(player, 20.0D, Attribute.MAX_HEALTH);
        var maxHealth = AttributeUtils.baseValue(player, Attribute.MAX_HEALTH);
        player.setHealth(maxHealth == null ? 20.0D : Math.max(1.0D, maxHealth));
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

        for (var base : spawns) {
            int count = (int) candidates.stream()
                    .filter(p -> p != null && p.isOnline())
                    .filter(_ -> base.getWorld() != null)
                    .filter(p -> p.getWorld().getUID().equals(base.getWorld().getUID()))
                    .filter(p -> p.getLocation().distanceSquared(base) <= r2)
                    .count();

            if (count < best) {
                best = count;
                bestList.clear();
                bestList.add(base);
            } else if (count == best) {
                bestList.add(base);
            }
        }

        var picked = bestList.get(ThreadLocalRandom.current().nextInt(bestList.size()));
        return picked.clone();
    }

}
