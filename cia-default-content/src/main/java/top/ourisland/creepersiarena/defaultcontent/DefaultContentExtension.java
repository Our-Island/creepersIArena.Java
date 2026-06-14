package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.ability.SimpleAbility;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.store.IStoreService;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;
import top.ourisland.creepersiarena.api.extension.ICiaExtension;
import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
import top.ourisland.creepersiarena.api.game.death.DeathCleanupParticipantId;
import top.ourisland.creepersiarena.api.game.death.DeathMessageProviderId;
import top.ourisland.creepersiarena.api.game.death.DeathResolverId;
import top.ourisland.creepersiarena.api.game.death.IDeathResolutionRegistry;
import top.ourisland.creepersiarena.api.game.mutation.IMutationRegistry;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.rest.IRestStateService;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.death.DamageAttributionStore;
import top.ourisland.creepersiarena.core.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.core.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.defaultcontent.economy.DefaultCurrencies;
import top.ourisland.creepersiarena.defaultcontent.economy.cosmetic.particle.DefaultParticleCosmetics;
import top.ourisland.creepersiarena.defaultcontent.economy.cosmetic.particle.ParticlePreviewDisplayService;
import top.ourisland.creepersiarena.defaultcontent.economy.store.DefaultParticleStore;
import top.ourisland.creepersiarena.defaultcontent.economy.store.DefaultParticleStoreAccessListener;
import top.ourisland.creepersiarena.defaultcontent.game.death.*;
import top.ourisland.creepersiarena.defaultcontent.game.mode.battle.BattleGameplayListener;
import top.ourisland.creepersiarena.defaultcontent.game.mode.battle.BattleRespawnEffectsListener;
import top.ourisland.creepersiarena.defaultcontent.game.mode.steal.runtime.StealGameplayListener;
import top.ourisland.creepersiarena.defaultcontent.game.mutation.acceleratedtime.AcceleratedTimeMutationEffect;
import top.ourisland.creepersiarena.defaultcontent.job.listener.SkillImplementationListener;
import top.ourisland.creepersiarena.defaultcontent.job.utils.BuiltinCombatUtils;

import java.util.function.LongSupplier;

/**
 * Entry point for CreepersIArena's bundled gameplay content.
 * <p>
 * The default content is intentionally loaded through the same annotation path as external CIA extension jars. This
 * keeps built-in jobs, skills and modes on the same registration surface that third-party content uses.
 */
@CiaExtensionInfo(
        id = "cia-default-content",
        namespace = "cia",
        name = "CreepersIArena Default Content",
        apiVersion = 1,
        authors = {"Our Island", "Chiloven945", "xqysp"},
        loadOrder = CiaExtensionLoadOrder.EARLY
)
public final class DefaultContentExtension implements ICiaExtension {

    private static final String ROOT_PACKAGE = "top.ourisland.creepersiarena";
    private ParticlePreviewDisplayService particlePreviewDisplays;

    @Override
    public void onLoad(ICiaExtensionContext context) {
        DefaultContentRuntimeIdentity.install(context);
        context.mergeYamlResource("default-content/config.yml", "config.yml");
        context.mergeYamlResource("default-content/arena.yml", "arena.yml");
        context.installResource("default-content/skill.yml", "skill.yml");
        context.installResource("default-content/death-messages.yml", "death-messages.yml");
        context.mergePropertiesResource("lang/en_us.properties", "lang/en_us.properties");
        context.mergePropertiesResource("lang/zh_cn.properties", "lang/zh_cn.properties");
        context.registerAnnotated(ROOT_PACKAGE);
    }

    @Override
    public void onEnable(ICiaExtensionContext context) {
        var sessions = context.requireService(PlayerSessionStore.class);
        var runtime = context.requireService(SkillRuntime.class);
        var tickTask = context.requireService(SkillTickTask.class);
        var gameManager = context.requireService(GameManager.class);
        var restState = context.getService(IRestStateService.class);

        BuiltinCombatUtils.installSessions(sessions);
        registerDefaultAbilities(context);
        registerMutationContent(context);
        registerDeathContent(context, sessions, runtime);
        registerEconomyStoreAndCosmetics(context);

        context.registerListener(
                new SkillImplementationListener(sessions, runtime, tickTask, () -> context.getService(IAbilityGate.class)),
                new BuiltinKillFeedbackService(gameManager),
                new BattleGameplayListener(gameManager, sessions),
                new BattleRespawnEffectsListener(
                        context.plugin(),
                        gameManager,
                        sessions,
                        context.requireService(IAbilityGate.class)
                ),
                new StealGameplayListener(gameManager, sessions, restState),
                new DefaultParticleStoreAccessListener(
                        context.requireService(IStoreService.class),
                        context.requireService(IAbilityGate.class),
                        context.plugin().getDataFolder().toPath().resolve("config.yml")
                )
        );
    }

    @Override
    public void onDisable(ICiaExtensionContext context) {
        if (particlePreviewDisplays != null) {
            particlePreviewDisplays.stop();
            particlePreviewDisplays = null;
        }
    }

    private void registerDefaultAbilities(ICiaExtensionContext context) {
        context.registerAbility(
                new SimpleAbility(DefaultContentAbilities.BATTLE_RESPAWN_RECOVERY),
                new SimpleAbility(DefaultContentAbilities.BATTLE_RESPAWN_VISUALS),
                new SimpleAbility(DefaultContentAbilities.BATTLE_BOSSBAR),
                new SimpleAbility(DefaultContentAbilities.BATTLE_MAP_PROGRESS_ROTATION),
                new SimpleAbility(DefaultContentAbilities.BATTLE_PROGRESS_FEEDBACK),
                new SimpleAbility(DefaultContentAbilities.KILL_FEEDBACK),
                new SimpleAbility(DefaultContentAbilities.BUILTIN_DEATH_CLEANUP),
                new SimpleAbility(DefaultContentAbilities.STEAL_WAITING_BOSSBAR),
                new SimpleAbility(DefaultContentAbilities.STEAL_SPECTATOR_TOUR),
                new SimpleAbility(DefaultContentAbilities.STEAL_CHOOSE_JOB_PHASE),
                new SimpleAbility(DefaultContentAbilities.STEAL_ROUND_BOSSBAR),
                new SimpleAbility(DefaultContentAbilities.STEAL_CELEBRATION_BOSSBAR),
                new SimpleAbility(DefaultContentAbilities.STEAL_SELECTION_BARRIERS),
                new SimpleAbility(DefaultContentAbilities.STEAL_OBJECTIVE_FEEDBACK),
                new SimpleAbility(DefaultContentAbilities.STEAL_CELEBRATION_FIREWORKS),
                new SimpleAbility(DefaultContentAbilities.PARTICLE_STORE),
                new SimpleAbility(DefaultContentAbilities.PARTICLE_COSMETICS),
                new SimpleAbility(DefaultContentAbilities.PARTICLE_PREVIEW_DISPLAYS)
        );
    }

    private void registerMutationContent(ICiaExtensionContext context) {
        context.requireService(IMutationRegistry.class).registerMutation(
                context.owner(),
                new AcceleratedTimeMutationEffect(
                        context.plugin(),
                        context.plugin().getSLF4JLogger()
                )
        );
    }

    private void registerDeathContent(
            ICiaExtensionContext context,
            PlayerSessionStore sessions,
            SkillRuntime runtime
    ) {
        var attributionStore = context.getService(DamageAttributionStore.class);
        LongSupplier currentTick = attributionStore == null ? () -> 0L : attributionStore::currentTick;
        var catalog = BuiltinDeathMessageCatalog.load(
                context.plugin().getDataFolder().toPath().resolve("death-messages.yml"),
                getClass().getClassLoader()
        );

        var registry = context.requireService(IDeathResolutionRegistry.class);
        registry.registerResolver(
                context.owner(),
                DeathResolverId.parse("cia:builtin_death_causes"),
                new BuiltinDeathCauseResolver(sessions, currentTick)
        );
        registry.registerMessageProvider(
                context.owner(),
                DeathMessageProviderId.parse("cia:builtin_death_messages"),
                new BuiltinDeathMessageProvider(catalog)
        );
        registry.registerCleanupParticipant(
                context.owner(),
                DeathCleanupParticipantId.parse("cia:builtin_death_cleanup"),
                new BuiltinDeathCleanupParticipant(runtime.store())
        );
    }

    private void registerEconomyStoreAndCosmetics(ICiaExtensionContext context) {
        DefaultCurrencies.register(context);
        DefaultParticleCosmetics.register(context);
        DefaultParticleStore.register(context);

        particlePreviewDisplays = new ParticlePreviewDisplayService(
                context.plugin(),
                context.requireService(IAbilityGate.class),
                context.requireService(ICosmeticRegistry.class),
                context.plugin().getDataFolder().toPath().resolve("config.yml")
        );
        particlePreviewDisplays.start();
    }

}
