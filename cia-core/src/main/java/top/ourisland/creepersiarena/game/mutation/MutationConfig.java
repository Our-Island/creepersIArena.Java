package top.ourisland.creepersiarena.game.mutation;

import org.bukkit.configuration.ConfigurationSection;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.IAbilityConfigView;

public record MutationConfig(
        int idleAttemptTicks,
        int failedRollCounterValue,
        int deathNudgeTicks,
        boolean requireOnlineTarget,
        top.ourisland.creepersiarena.api.game.mutation.MutationClockMode clockMode,
        int maxLogicalStepsPerRun,
        MutationSelectionConfig selection
) {

    public MutationConfig {
        idleAttemptTicks = Math.max(1, idleAttemptTicks);
        failedRollCounterValue = Math.clamp(failedRollCounterValue, 0, idleAttemptTicks - 1);
        deathNudgeTicks = Math.max(0, deathNudgeTicks);
        if (clockMode == null) clockMode = top.ourisland.creepersiarena.api.game.mutation.MutationClockMode.AUTO;
        maxLogicalStepsPerRun = Math.max(1, maxLogicalStepsPerRun);
        if (selection == null) selection = MutationSelectionConfig.defaults();
    }

    public static MutationConfig load(
            IAbilityConfigView view,
            Logger logger
    ) {
        try {
            var section = view == null ? null : view.settingsSection();
            return section == null ? defaults() : fromSection(section);
        } catch (Throwable t) {
            if (logger != null)
                logger.warn("[Mutation] Failed to load mutation ability settings: {}", t.getMessage(), t);
            return defaults();
        }
    }

    public static MutationConfig defaults() {
        return new MutationConfig(
                6000,
                500,
                1,
                true,
                top.ourisland.creepersiarena.api.game.mutation.MutationClockMode.AUTO,
                8,
                MutationSelectionConfig.defaults()
        );
    }

    public static MutationConfig fromSection(ConfigurationSection section) {
        int idleAttemptTicks = Math.max(1, section.getInt("idle-attempt-ticks", 6000));
        int failedValue = section.getInt("failed-roll-counter-value", 500);

        return new MutationConfig(
                idleAttemptTicks,
                failedValue,
                section.getInt("death-nudge-ticks", 1),
                section.getBoolean("require-online-target", true),
                top.ourisland.creepersiarena.api.game.mutation.MutationClockMode.fromConfig(section.getString("clock-mode", "AUTO")),
                section.getInt("max-logical-steps-per-run", 8),
                selection(section.getConfigurationSection("selection"))
        );
    }

    private static MutationSelectionConfig selection(ConfigurationSection section) {
        if (section == null) return MutationSelectionConfig.defaults();
        return new MutationSelectionConfig(
                section.getInt("online-roll-min", -5),
                section.getInt("online-roll-max", 10),
                section.getInt("offline-roll-min", -5),
                section.getInt("offline-roll-max", 0),
                section.getInt("start-min-inclusive", 1)
        );
    }

}
