package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.command.AdminRuntimeState;
import top.ourisland.creepersiarena.command.CiaCommand;
import top.ourisland.creepersiarena.command.service.LeaveService;
import top.ourisland.creepersiarena.command.service.UserLanguageService;

public final class CommandModule implements BootstrapModule {

    @Override
    public String name() {
        return "command";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            rt.putService(AdminRuntimeState.class, new AdminRuntimeState());
            rt.putService(UserLanguageService.class, new UserLanguageService(rt.plugin()));
            rt.putService(LeaveService.class, new LeaveService(rt));

            rt.plugin().getLifecycleManager().registerEventHandler(
                    LifecycleEvents.COMMANDS, event ->
                            CiaCommand.register(rt, event.registrar())
            );
        }, "Registering commands...", "Commands registered.");
    }
}
