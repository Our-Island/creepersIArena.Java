package top.ourisland.creepersiarena.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.game.listener.*;
import top.ourisland.creepersiarena.job.listener.SkillImplementationListener;
import top.ourisland.creepersiarena.job.listener.SkillUiListener;

public final class ListenerBootstrap {

    public void register(BootstrapContext ctx) {
        Logger log = ctx.plugin().getSLF4JLogger();
        PluginManager pm = Bukkit.getPluginManager();

        log.info("[Bootstrap] Register listener: ArenaDeathListener.");
        pm.registerEvents(new ArenaDeathListener(
                ctx.sessionStore(),
                ctx.flow()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: SkillImplementationListener.");
        pm.registerEvents(new SkillImplementationListener(), ctx.plugin());

        log.info("[Bootstrap] Register listener: LobbyEntryListener.");
        pm.registerEvents(new LobbyEntryListener(
                ctx.plugin(),
                log,
                ctx.lobbyService(),
                ctx.sessionStore(),
                ctx.flow()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: LobbyUiListener.");
        pm.registerEvents(new LobbyUiListener(
                ctx.lobbyItemCodec(),
                ctx.sessionStore(),
                ctx.flow()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: PlayerConnectionListener.");
        pm.registerEvents(new PlayerConnectionListener(
                ctx.plugin(),
                log,
                ctx.flow()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: PlayerStateRulesListener.");
        pm.registerEvents(new PlayerStateRulesListener(
                ctx.sessionStore(),
                ctx.lobbyService()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: SkillUiListener.");
        pm.registerEvents(new SkillUiListener(
                ctx.sessionStore(),
                ctx.skillCodec(),
                ctx.skillRuntime(),
                ctx.skillNowTick()
        ), ctx.plugin());

        log.info("[Bootstrap] All listeners registered.");
    }
}
