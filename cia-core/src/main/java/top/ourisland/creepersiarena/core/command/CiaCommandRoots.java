package top.ourisland.creepersiarena.core.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.AdminHandlers;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.handler.PlayerHandlers;
import top.ourisland.creepersiarena.core.command.tree.AdminCommandTree;
import top.ourisland.creepersiarena.core.command.tree.PlayerCommandTree;

/**
 * Owns the shared handler instances used to build player and admin command roots.
 */
public final class CiaCommandRoots {

    private final PlayerCommandTree playerTree;
    private final AdminCommandTree adminTree;

    public CiaCommandRoots(BootstrapRuntime rt) {
        var context = new CommandHandlerContext(rt);
        var playerHandlers = new PlayerHandlers(context);
        var adminHandlers = new AdminHandlers(context);

        this.adminTree = new AdminCommandTree(rt, adminHandlers);
        this.playerTree = new PlayerCommandTree(rt, playerHandlers);
    }

    public LiteralArgumentBuilder<CommandSourceStack> playerRoot() {
        return playerTree.build(CiaCommandConstants.PLAYER_ROOT_LITERAL);
    }

    public LiteralArgumentBuilder<CommandSourceStack> adminRoot(String literal) {
        return adminTree.build(literal);
    }

}
