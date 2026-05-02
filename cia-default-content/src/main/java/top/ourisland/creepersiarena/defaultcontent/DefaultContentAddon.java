package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.CiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtension;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.job.listener.SkillImplementationListener;
import top.ourisland.creepersiarena.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.job.utils.BuiltinCombatUtils;

/**
 * Entry point for CreepersIArena's bundled gameplay content.
 * <p>
 * The default content is intentionally loaded through the same annotation path as external CIA extension jars. This
 * keeps built-in jobs, skills and modes on the same registration surface that third-party content uses.
 */
public final class DefaultContentAddon implements CiaExtension {

    private static final String ROOT_PACKAGE = "top.ourisland.creepersiarena";

    @Override
    public void onLoad(CiaExtensionContext context) {
        context.registerAnnotated(ROOT_PACKAGE);
    }

    @Override
    public void onEnable(CiaExtensionContext context) {
        var sessions = context.requireService(PlayerSessionStore.class);
        var runtime = context.requireService(SkillRuntime.class);
        var tickTask = context.requireService(SkillTickTask.class);

        BuiltinCombatUtils.installSessions(sessions);
        context.registerListener(new SkillImplementationListener(sessions, runtime, tickTask));
    }

}
