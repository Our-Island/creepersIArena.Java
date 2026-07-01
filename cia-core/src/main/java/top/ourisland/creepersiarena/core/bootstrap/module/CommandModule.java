package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.command.AdminRuntimeState;
import top.ourisland.creepersiarena.core.command.CiaCommandRegistrar;
import top.ourisland.creepersiarena.core.command.service.LeaveService;
import top.ourisland.creepersiarena.core.command.service.PlayerPreferenceService;
import top.ourisland.creepersiarena.core.command.service.UserLanguageService;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.player.PlayerDataService;

@CiaBootstrapModule(name = "command", order = 1100)
public final class CommandModule implements IBootstrapModule {

    @Override
    public String name() {
        return "command";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            rt.putService(AdminRuntimeState.class, new AdminRuntimeState());
            rt.putService(PlayerPreferenceService.class, new PlayerPreferenceService(
                    rt.log(),
                    rt.requireService(JdbcDatabaseService.class),
                    rt.requireService(PlayerDataService.class)
            ));
            rt.putService(UserLanguageService.class, new UserLanguageService(
                    rt.log(),
                    rt.requireService(JdbcDatabaseService.class),
                    rt.requireService(PlayerDataService.class)
            ));
            rt.putService(LeaveService.class, new LeaveService(rt));

            rt.plugin().getLifecycleManager().registerEventHandler(
                    LifecycleEvents.COMMANDS, event ->
                            CiaCommandRegistrar.register(rt, event.registrar())
            );
        }, "Registering command lifecycle handler...", "Command lifecycle handler registered.");
    }

}
