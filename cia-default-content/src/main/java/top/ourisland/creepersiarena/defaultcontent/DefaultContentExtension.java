package top.ourisland.creepersiarena.defaultcontent;

import org.bukkit.World;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.ability.SimpleAbility;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;
import top.ourisland.creepersiarena.api.extension.ICiaExtension;
import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
import top.ourisland.creepersiarena.api.game.death.IDeathResolutionRegistry;
import top.ourisland.creepersiarena.api.game.mutation.IMutationRegistry;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.defaultcontent.death.*;
import top.ourisland.creepersiarena.defaultcontent.mutation.acceleratedtime.AcceleratedTimeMutationEffect;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.death.DamageAttributionStore;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleGameplayListener;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleRespawnPresentation;
import top.ourisland.creepersiarena.game.mode.impl.steal.runtime.StealGameplayListener;
import top.ourisland.creepersiarena.game.regeneration.RegenerationService;
import top.ourisland.creepersiarena.job.listener.SkillImplementationListener;
import top.ourisland.creepersiarena.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.job.utils.BuiltinCombatUtils;

import java.util.function.LongSupplier;

/**
 * Entry point for CreepersIArena's bundled gameplay content.
 * <p>
 * The default content is intentionally loaded through the same annotation path as external CIA extension jars. This
 * keeps built-in jobs, skills and modes on the same registration surface that third-party content uses.
 */
@CiaExtensionInfo(
        id = "cia-default-content",
        name = "CreepersIArena Default Content",
        apiVersion = 1,
        authors = {"Our Island", "Chiloven945", "xqysp"},
        loadOrder = CiaExtensionLoadOrder.EARLY
)
public final class DefaultContentExtension implements ICiaExtension {

    private static final String ROOT_PACKAGE = "top.ourisland.creepersiarena";

    @Override
    public void onLoad(ICiaExtensionContext context) {
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
        var regeneration = context.getService(RegenerationService.class);

        BuiltinCombatUtils.installSessions(sessions);
        registerDefaultAbilities(context);
        registerMutationContent(context);
        registerDeathContent(context, sessions, runtime);

        context.registerListener(
                new SkillImplementationListener(sessions, runtime, tickTask),
                new BuiltinKillFeedbackService(gameManager),
                new BattleGameplayListener(gameManager, sessions),
                new BattleRespawnPresentation(context.plugin(), gameManager, sessions),
                new StealGameplayListener(gameManager, sessions, regeneration)
        );
    }

    private void registerDefaultAbilities(ICiaExtensionContext context) {
        context.registerAbility(
                new SimpleAbility(DefaultContentAbilities.BATTLE_RESPAWN_PRESENTATION),
                new SimpleAbility(DefaultContentAbilities.BATTLE_BOSSBAR),
                new SimpleAbility(DefaultContentAbilities.BATTLE_MAP_ROTATION),
                new SimpleAbility(DefaultContentAbilities.BATTLE_PROGRESS_FEEDBACK),
                new SimpleAbility(DefaultContentAbilities.KILL_FEEDBACK),
                new SimpleAbility(DefaultContentAbilities.STEAL_WAITING_BOSSBAR),
                new SimpleAbility(DefaultContentAbilities.STEAL_SPECTATOR_TOUR),
                new SimpleAbility(DefaultContentAbilities.STEAL_CHOOSE_JOB_PHASE),
                new SimpleAbility(DefaultContentAbilities.STEAL_ROUND_BOSSBAR),
                new SimpleAbility(DefaultContentAbilities.STEAL_CELEBRATION_BOSSBAR),
                new SimpleAbility(DefaultContentAbilities.STEAL_SELECTION_BARRIERS),
                new SimpleAbility(DefaultContentAbilities.STEAL_CELEBRATION_FIREWORKS)
        );
    }

    private void registerMutationContent(ICiaExtensionContext context) {
        var world = context.getService(World.class);
        if (world == null) return;
        context.requireService(IMutationRegistry.class).registerMutation(
                context.extensionId(),
                new AcceleratedTimeMutationEffect(
                        context.plugin(),
                        world,
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
        registry.registerResolver(context.extensionId(), new BuiltinDeathCauseResolver(sessions, currentTick));
        registry.registerMessageProvider(context.extensionId(), new BuiltinDeathMessageProvider(catalog));
        registry.registerCleanupParticipant(context.extensionId(), new BuiltinDeathCleanupParticipant(runtime.store()));
    }

}
