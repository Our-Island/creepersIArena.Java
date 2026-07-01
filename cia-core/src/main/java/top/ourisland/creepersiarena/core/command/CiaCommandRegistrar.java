package top.ourisland.creepersiarena.core.command;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;

import java.util.List;

/**
 * Registers the player/admin command roots and standalone redirects.
 */
public final class CiaCommandRegistrar {

    private CiaCommandRegistrar() {
    }

    public static void register(BootstrapRuntime rt, Commands commands) {
        var roots = new CiaCommandRoots(rt);

        var playerRoot = roots.playerRoot().build();
        commands.register(
                playerRoot,
                "CreepersIArena commands",
                CiaCommandConstants.PLAYER_ROOT_ALIASES
        );

        registerRedirect(commands, "join", child(playerRoot, "join"), null, List.of());
        registerRedirect(commands, "leave", child(playerRoot, "leave"), null, List.of());
        registerRedirect(commands, "job", child(playerRoot, "job"), CiaPermissions.COMMAND_JOB, List.of());
        registerRedirect(commands, "team", child(playerRoot, "team"), CiaPermissions.COMMAND_TEAM, List.of("cteam"));
        registerRedirect(commands, "language", child(playerRoot, "language"), CiaPermissions.COMMAND_LANGUAGE, List.of());
        registerRedirect(commands, "preference", child(playerRoot, "preference"), CiaPermissions.COMMAND_PREFERENCE, List.of("pref"));
        registerRedirect(commands, "choosejob", child(playerRoot, "choosejob"), CiaPermissions.CHOOSEJOB, List.of());
        registerRedirect(commands, "balance", child(playerRoot, "balance"), CiaPermissions.COMMAND_BALANCE, List.of("bal"));
        registerRedirect(commands, "store", child(playerRoot, "store"), CiaPermissions.COMMAND_STORE, List.of());
        registerRedirect(commands, "particles", child(playerRoot, "particles"), CiaPermissions.COMMAND_PARTICLES, List.of("particle"));

        var adminRoot = roots.adminRoot(CiaCommandConstants.ADMIN_ROOT_LITERAL).build();
        commands.register(adminRoot, "CreepersIArena admin commands", CiaCommandConstants.NO_ALIASES);

        rt.log().info("[Command] Registered command trees and redirects.");
    }

    private static void registerRedirect(
            Commands commands,
            String literal,
            CommandNode<CommandSourceStack> target,
            String permission,
            List<String> aliases
    ) {
        var builder = Commands.literal(literal);
        if (permission != null && !permission.isBlank()) {
            builder = builder.requires(source -> source.getSender().hasPermission(permission));
        }
        commands.register(
                builder.redirect(target).build(),
                "redirect:" + literal,
                aliases == null
                        ? List.of()
                        : aliases
        );
    }

    private static CommandNode<CommandSourceStack> child(
            LiteralCommandNode<CommandSourceStack> root,
            String name
    ) {
        var child = root.getChild(name);
        if (child == null) throw new IllegalStateException("Missing command child: " + name);
        return child;
    }

}
