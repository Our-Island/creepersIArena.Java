package top.ourisland.creepersiarena.core.command.handler;

import lombok.Getter;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.message.CommandHelpRenderer;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;

/**
 * Shared command handler dependencies. Concrete handlers receive this context so command trees do not need to know
 * about runtime services or message renderer wiring.
 */
@Getter
public final class CommandHandlerContext {

    private final BootstrapRuntime runtime;
    private final CommandMessenger messenger;
    private final CommandHelpRenderer helpRenderer;

    public CommandHandlerContext(BootstrapRuntime runtime) {
        this(runtime, new CommandMessenger());
    }

    public CommandHandlerContext(
            BootstrapRuntime runtime,
            CommandMessenger messenger
    ) {
        this.runtime = runtime;
        this.messenger = messenger;
        this.helpRenderer = new CommandHelpRenderer(messenger);
    }

}
