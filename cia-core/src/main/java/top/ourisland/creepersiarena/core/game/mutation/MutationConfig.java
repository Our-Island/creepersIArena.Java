package top.ourisland.creepersiarena.core.game.mutation;

import org.bukkit.configuration.ConfigurationSection;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.IAbilityConfigView;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.game.mutation.MutationClockMode;

public record MutationConfig(
        int idleAttemptTicks,
        int failedRollCounterValue,
        int deathNudgeTicks,
        boolean requireOnlineTarget,
        MutationClockMode clockMode,
        int maxLogicalStepsPerRun,
        MutationSelectionConfig selection
) {

    public MutationConfig {
        if (idleAttemptTicks <= 0) {
            throw new IllegalArgumentException("game.abilities.core.mutation.settings.idle-attempt-ticks must be positive");
        }
        if (failedRollCounterValue < 0 || failedRollCounterValue >= idleAttemptTicks) {
            throw new IllegalArgumentException(
                    "game.abilities.core.mutation.settings.failed-roll-counter-value must be between 0 and "
                            + (idleAttemptTicks - 1)
            );
        }
        if (deathNudgeTicks < 0) {
            throw new IllegalArgumentException("game.abilities.core.mutation.settings.death-nudge-ticks must be >= 0");
        }
        if (clockMode == null) {
            throw new IllegalArgumentException("Mutation clock mode is required");
        }
        if (maxLogicalStepsPerRun <= 0) {
            throw new IllegalArgumentException(
                    "game.abilities.core.mutation.settings.max-logical-steps-per-run must be positive"
            );
        }
        if (selection == null) {
            throw new IllegalArgumentException("Mutation selection config is required");
        }
    }

    public static MutationConfig load(
            IAbilityConfigView view,
            Logger logger
    ) {
        var section = view == null ? null : view.settingsSection();
        return section == null ? defaults() : fromSection(section);
    }

    public static MutationConfig defaults() {
        return new MutationConfig(
                6000,
                500,
                1,
                true,
                MutationClockMode.AUTO,
                8,
                MutationSelectionConfig.defaults()
        );
    }

    public static MutationConfig fromSection(ConfigurationSection section) {
        var root = "game.abilities.core.mutation.settings";
        int idleAttemptTicks = StrictConfig.integer(section, "idle-attempt-ticks", 6000, root + ".idle-attempt-ticks");
        int failedValue = StrictConfig.integer(
                section,
                "failed-roll-counter-value",
                500,
                root + ".failed-roll-counter-value"
        );

        return new MutationConfig(
                idleAttemptTicks,
                failedValue,
                StrictConfig.integer(section, "death-nudge-ticks", 1, root + ".death-nudge-ticks"),
                StrictConfig.bool(section, "require-online-target", true, root + ".require-online-target"),
                MutationClockMode.fromConfig(
                        StrictConfig.string(section, "clock-mode", "AUTO", root + ".clock-mode")
                ),
                StrictConfig.integer(
                        section,
                        "max-logical-steps-per-run",
                        8,
                        root + ".max-logical-steps-per-run"
                ),
                selection(StrictConfig.section(section, "selection", root + ".selection"), root + ".selection")
        );
    }

    private static MutationSelectionConfig selection(ConfigurationSection section, String root) {
        if (section == null) return MutationSelectionConfig.defaults();
        return new MutationSelectionConfig(
                StrictConfig.integer(section, "online-roll-min", -5, root + ".online-roll-min"),
                StrictConfig.integer(section, "online-roll-max", 10, root + ".online-roll-max"),
                StrictConfig.integer(section, "offline-roll-min", -5, root + ".offline-roll-min"),
                StrictConfig.integer(section, "offline-roll-max", 0, root + ".offline-roll-max"),
                StrictConfig.integer(section, "start-min-inclusive", 1, root + ".start-min-inclusive")
        );
    }

}
