package top.ourisland.creepersiarena.core.bootstrap.module;

import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.permission.LuckPermsService;

/**
 * Registers permissions in code and optionally hooks LuckPerms.
 */
public final class PermissionModule implements IBootstrapModule {

    @Override
    public String name() {
        return "permission";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            Logger log = rt.log();
            CiaPermissions.registerAll(rt.plugin());

            // IMPORTANT: do not load LuckPerms API classes unless the plugin is present.
            boolean lpEnabled = rt.plugin().getServer().getPluginManager().isPluginEnabled("LuckPerms");
            if (!lpEnabled) {
                log.info("[LuckPerms] Not installed; using built-in permission system.");
                return;
            }

            LuckPermsService lp = LuckPermsService.tryLoad(rt.plugin(), log);
            if (lp != null) {
                rt.putService(LuckPermsService.class, lp);
                log.info("[LuckPerms] Hooked LuckPerms API (server permissions will be handled by LuckPerms). ");
            } else {
                log.warn("[LuckPerms] Enabled but API could not be resolved. Is LuckPerms fully started?");
            }
        }, "Registering permissions / hooks...", "done");
    }
}
