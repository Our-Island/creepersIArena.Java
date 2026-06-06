package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;
import top.ourisland.creepersiarena.api.extension.ICiaExtension;
import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.defaultcontent.death.*;
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
        registerDeathContent(context, sessions, runtime);

        context.registerListener(new SkillImplementationListener(sessions, runtime, tickTask));
        context.registerListener(new BuiltinKillFeedbackService());
        context.registerListener(new BattleGameplayListener(gameManager, sessions));
        context.registerListener(new BattleRespawnPresentation(context.plugin(), gameManager, sessions));
        context.registerListener(new StealGameplayListener(gameManager, sessions, regeneration));
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

        context.registerDeathCauseResolver(new BuiltinDeathCauseResolver(sessions, currentTick));
        context.registerDeathMessageProvider(new BuiltinDeathMessageProvider(catalog));
        context.registerDeathCleanupParticipant(new BuiltinDeathCleanupParticipant(runtime.store()));
    }

}
