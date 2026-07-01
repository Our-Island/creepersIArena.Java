package top.ourisland.creepersiarena.core.command;

import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;

/**
 * Backwards-compatible command entrypoint.
 *
 * <p>The actual command tree is now composed by {@link CiaCommandRegistrar} and
 * the classes under {@code core.command.tree}. Keeping this class preserves the existing bootstrap API used by
 * {@code CommandModule}.</p>
 */
public final class CiaCommand {

    private CiaCommand() {
    }

    public static void register(BootstrapRuntime rt, Commands commands) {
        CiaCommandRegistrar.register(rt, commands);
    }

}
