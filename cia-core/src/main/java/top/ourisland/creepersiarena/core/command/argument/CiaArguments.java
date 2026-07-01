package top.ourisland.creepersiarena.core.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.command.CommandParsers;
import top.ourisland.creepersiarena.core.command.model.ConfigTarget;

/**
 * Common Brigadier argument helpers shared by command-tree classes.
 */
public final class CiaArguments {

    private CiaArguments() {
    }

    public static CommandSender sender(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getSender();
    }

    public static boolean hasPermission(CommandSourceStack source, String permission) {
        return source.getSender().hasPermission(permission);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> word(String name) {
        return Commands.argument(name, StringArgumentType.word());
    }

    public static RequiredArgumentBuilder<CommandSourceStack, CiaKey> ciaKey(String name) {
        return Commands.argument(name, CiaKeyArgument.ciaKey());
    }

    public static JobId jobId(CommandContext<CommandSourceStack> ctx, String name) {
        return JobId.of(CiaKeyArgument.get(ctx, name));
    }

    public static CurrencyId currencyId(CommandContext<CommandSourceStack> ctx, String name) {
        return CurrencyId.of(CiaKeyArgument.get(ctx, name));
    }

    public static StoreId storeId(CommandContext<CommandSourceStack> ctx, String name) {
        return StoreId.of(CiaKeyArgument.get(ctx, name));
    }

    public static CosmeticId cosmeticId(CommandContext<CommandSourceStack> ctx, String name) {
        return CosmeticId.of(CiaKeyArgument.get(ctx, name));
    }

    public static GameModeId modeId(CommandContext<CommandSourceStack> ctx, String name) {
        return GameModeId.of(CiaKeyArgument.get(ctx, name));
    }

    public static AbilityId abilityId(CommandContext<CommandSourceStack> ctx, String name) {
        return AbilityId.of(CiaKeyArgument.get(ctx, name));
    }

    public static ArenaId arenaId(CommandContext<CommandSourceStack> ctx, String name) {
        return ArenaId.parse(StringArgumentType.getString(ctx, name));
    }

    public static TeamId teamId(CommandContext<CommandSourceStack> ctx, String name) {
        return CommandParsers.parseTeamId(StringArgumentType.getString(ctx, name));
    }

    public static ConfigTarget configTarget(CommandContext<CommandSourceStack> ctx, String name) {
        return ConfigTarget.parse(StringArgumentType.getString(ctx, name));
    }

}
