package top.ourisland.creepersiarena.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.game.listener.*;
import top.ourisland.creepersiarena.job.listener.SkillHotbarLockListener;
import top.ourisland.creepersiarena.job.listener.SkillTriggerListener;

public final class ListenerBootstrap {

    public void register(BootstrapContext ctx) {
        Logger log = ctx.plugin().getSLF4JLogger();
        PluginManager pm = Bukkit.getPluginManager();

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

        log.info("[Bootstrap] Register listener: HubActionListener.");
        pm.registerEvents(new HubActionListener(
                ctx.lobbyItemCodec(),
                ctx.sessionStore(),
                ctx.flow()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: LobbyInventoryClickListener.");
        pm.registerEvents(new LobbyInventoryClickListener(
                ctx.lobbyItemCodec(),
                ctx.sessionStore(),
                ctx.flow()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: ArenaDeathListener.");
        pm.registerEvents(new ArenaDeathListener(
                ctx.sessionStore(),
                ctx.flow()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: SkillTriggerListener.");
        pm.registerEvents(new SkillTriggerListener(
                ctx.skillItemCodec(),
                ctx.skillEngine(),
                ctx.skillContextFactory()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: SkillHotbarLockListener.");
        pm.registerEvents(new SkillHotbarLockListener(
                ctx.skillItemCodec()
        ), ctx.plugin());

        log.info("[Bootstrap] Register listener: LobbyEntryListener.");
        pm.registerEvents(new LobbyEntryListener(
                ctx.plugin(),
                log,
                ctx.lobbyService(),
                ctx.sessionStore(),
                ctx.flow()
        ), ctx.plugin());

        log.info("[Bootstrap] All listeners registered.");
    }
}
