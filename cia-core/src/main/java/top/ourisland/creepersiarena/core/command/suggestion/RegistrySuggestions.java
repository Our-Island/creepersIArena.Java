package top.ourisland.creepersiarena.core.command.suggestion;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.extension.loading.CiaExtensionManager;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.arena.ArenaManager;
import top.ourisland.creepersiarena.core.job.JobManager;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Suggestions sourced from runtime registries and managers.
 */
public final class RegistrySuggestions {

    private RegistrySuggestions() {
    }

    public static CompletableFuture<Suggestions> jobIds(
            BootstrapRuntime rt,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var jobs = rt.getService(JobManager.class);
        if (jobs == null) return builder.buildFuture();
        var ids = jobs.getAllJobIds().stream()
                .map(CiaResourceId::asString)
                .sorted(String::compareToIgnoreCase)
                .toList();
        return CiaSuggestions.withPrefix(builder, ids);
    }

    public static CompletableFuture<Suggestions> modeIds(
            BootstrapRuntime rt,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var games = rt.getService(GameManager.class);
        if (games == null) return builder.buildFuture();
        var ids = games.modes().keySet().stream()
                .map(Object::toString)
                .sorted(String::compareToIgnoreCase)
                .toList();
        return CiaSuggestions.withPrefix(builder, ids);
    }

    public static CompletableFuture<Suggestions> arenaIds(
            BootstrapRuntime rt,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var arenas = rt.getService(ArenaManager.class);
        if (arenas == null) return builder.buildFuture();
        var ids = arenas.arenas().stream()
                .map(arena -> arena.id().value())
                .sorted(String::compareToIgnoreCase)
                .toList();
        return CiaSuggestions.withPrefix(builder, ids);
    }

    public static CompletableFuture<Suggestions> currencyIds(
            BootstrapRuntime rt,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var registry = rt.getService(ICurrencyRegistry.class);
        if (registry == null) return builder.buildFuture();
        var ids = registry.currencies().stream()
                .map(currency -> currency.id().asString())
                .sorted(String::compareToIgnoreCase)
                .toList();
        return CiaSuggestions.withPrefix(builder, ids);
    }

    public static CompletableFuture<Suggestions> storeIds(
            BootstrapRuntime rt,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var registry = rt.getService(IStoreRegistry.class);
        if (registry == null) return builder.buildFuture();
        var ids = registry.stores().stream()
                .map(store -> store.id().asString())
                .sorted(String::compareToIgnoreCase)
                .toList();
        return CiaSuggestions.withPrefix(builder, ids);
    }

    public static CompletableFuture<Suggestions> cosmeticIds(
            BootstrapRuntime rt,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var registry = rt.getService(ICosmeticRegistry.class);
        if (registry == null) return builder.buildFuture();
        var ids = registry.cosmetics(null).stream()
                .map(cosmetic -> cosmetic.id().asString())
                .sorted(String::compareToIgnoreCase)
                .toList();
        return CiaSuggestions.withPrefix(builder, ids);
    }

    public static CompletableFuture<Suggestions> abilityIds(
            BootstrapRuntime rt,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) return builder.buildFuture();
        var ids = admin.abilityIds().stream()
                .map(AbilityId::asString)
                .sorted(String::compareToIgnoreCase)
                .toList();
        return CiaSuggestions.withPrefix(builder, ids);
    }

    public static CompletableFuture<Suggestions> extensionIds(
            BootstrapRuntime rt,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var manager = rt.getService(CiaExtensionManager.class);
        if (manager == null) return builder.buildFuture();

        var ids = manager.loadedExtensions().stream()
                .map(loaded -> loaded.descriptor().id().value())
                .collect(Collectors.toCollection(ArrayList::new));
        manager.loadFailures().stream()
                .map(f -> f.id().value())
                .forEach(ids::add);
        ids.sort(String::compareToIgnoreCase);
        return CiaSuggestions.withPrefix(builder, ids);
    }

}
