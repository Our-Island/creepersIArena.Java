package top.ourisland.creepersiarena.core.command.suggestion

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import top.ourisland.creepersiarena.api.ability.AbilityId
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.extension.loading.CiaExtensionManager
import top.ourisland.creepersiarena.core.game.GameManager
import top.ourisland.creepersiarena.core.game.arena.ArenaManager
import top.ourisland.creepersiarena.core.job.JobManager
import java.util.concurrent.CompletableFuture

/**
 * Suggestions sourced from runtime registries and managers.
 */
object RegistrySuggestions {

    @JvmStatic
    fun jobIds(
        rt: BootstrapRuntime?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val jobs = rt.getService(JobManager::class.java) ?: return builder.buildFuture()
        val ids = jobs.allJobIds
            .map { it.asString() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, ids)
    }

    @JvmStatic
    fun modeIds(
        rt: BootstrapRuntime?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val games = rt.getService(GameManager::class.java) ?: return builder.buildFuture()
        val ids = games.modes().keys
            .map { it.toString() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, ids)
    }

    @JvmStatic
    fun arenaIds(
        rt: BootstrapRuntime?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val arenas = rt.getService(ArenaManager::class.java) ?: return builder.buildFuture()
        val ids = arenas.arenas()
            .map { arena -> arena.id().value() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, ids)
    }

    @JvmStatic
    fun currencyIds(
        rt: BootstrapRuntime?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val registry = rt.getService(ICurrencyRegistry::class.java) ?: return builder.buildFuture()
        val ids = registry.currencies()
            .map { currency -> currency.id().asString() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, ids)
    }

    @JvmStatic
    fun storeIds(
        rt: BootstrapRuntime?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val registry = rt.getService(IStoreRegistry::class.java) ?: return builder.buildFuture()
        val ids = registry.stores()
            .map { store -> store.id().asString() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, ids)
    }

    @JvmStatic
    fun cosmeticIds(
        rt: BootstrapRuntime?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val registry = rt.getService(ICosmeticRegistry::class.java) ?: return builder.buildFuture()
        val ids = registry.cosmetics(null)
            .map { cosmetic -> cosmetic.id().asString() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, ids)
    }

    @JvmStatic
    fun abilityIds(
        rt: BootstrapRuntime?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val admin = rt.getService(IAbilityAdmin::class.java) ?: return builder.buildFuture()
        val ids = admin.abilityIds()
            .map(AbilityId::asString)
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, ids)
    }

    @JvmStatic
    fun extensionIds(
        rt: BootstrapRuntime?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val manager = rt.getService(CiaExtensionManager::class.java) ?: return builder.buildFuture()

        val ids = manager.loadedExtensions()
            .map { loaded -> loaded.descriptor().id().value() }
            .toMutableList()
        manager.loadFailures()
            .map { failure -> failure.id().value() }
            .forEach(ids::add)

        ids.sortWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, ids)
    }

}
