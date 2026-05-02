package top.ourisland.creepersiarena.game.mode.impl.steal;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;
import top.ourisland.creepersiarena.defaultcontent.DefaultLoadoutService;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer;
import top.ourisland.creepersiarena.utils.Msg;

/**
 * Player flow for the bundled steal mode.
 */
public final class StealPlayerFlow implements IModePlayerFlow {

    private final DefaultLoadoutService kit;

    public StealPlayerFlow(GameRuntime runtime) {
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
        String teamKey = ctx.session().selectedTeamKey();
        if (teamKey != null) {
            var group = arena.spawnGroup(teamKey);
            if (!group.isEmpty()) {
                return group.getFirst().clone();
            }
        }
        return arena.firstSpawnOrAnchor("default");
    }

    @Override
    public void onEnterGame(ModePlayerContext ctx) {
        Player player = ctx.player();
        player.setGameMode(GameMode.ADVENTURE);
        kit.apply(player, ctx.session());
        Msg.actionBar(player, Component.text("进入游戏"));
    }

}
