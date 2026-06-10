package top.ourisland.creepersiarena.defaultcontent.mutation.acceleratedtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.mutation.IMutationEffect;
import top.ourisland.creepersiarena.api.game.mutation.IMutationEffectContext;
import top.ourisland.creepersiarena.api.game.mutation.MutationStartResult;
import top.ourisland.creepersiarena.api.game.mutation.MutationType;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class AcceleratedTimeMutationEffect implements IMutationEffect {

    public static final MutationType TYPE = MutationType.of("accelerated_time");

    private final AcceleratedTimeTickRateController tickRateController;
    private final AcceleratedTimeWorldController worldController;
    private final AcceleratedTimeAttributeController attributeController;

    private AcceleratedTimeMutationConfig config = AcceleratedTimeMutationConfig.defaults();
    private double activeRate = 20.0D;
    private boolean physicalTickRateApplied;

    public AcceleratedTimeMutationEffect(
            Plugin plugin,
            World world,
            Logger logger
    ) {
        this.tickRateController = new AcceleratedTimeTickRateController(logger);
        this.worldController = new AcceleratedTimeWorldController(world, logger);
        this.attributeController = new AcceleratedTimeAttributeController(plugin, logger);
    }

    @Override
    public MutationType type() {
        return TYPE;
    }

    @Override
    public void reload(
            ConfigurationSection mutationSection,
            Logger logger
    ) {
        config = AcceleratedTimeMutationConfig.fromMutationSection(mutationSection);
    }

    @Override
    public boolean enabled() {
        return config.enabled();
    }

    @Override
    public MutationStartResult start(IMutationEffectContext context) {
        activeRate = randomRate(config.tickRateMin(), config.tickRateMax());
        physicalTickRateApplied = switch (context.clockMode()) {
            case VANILLA_TICK_RATE, AUTO -> tickRateController.applyTickRate(activeRate);
            case LOGICAL -> false;
        };

        worldController.onStart(config);
        var speedTargets = context.targets(config.speedTargetScope());
        attributeController.ensureApplied(speedTargets, config.movementSpeedAdd());
        broadcast(speedTargets, config.messages().randomStart());
        play(speedTargets, config.startSound());

        return new MutationStartResult(config.durationTicks());
    }

    @Override
    public void tick(
            IMutationEffectContext context,
            int syntheticSteps
    ) {
        Collection<Player> speedTargets = context.targets(config.speedTargetScope());
        Collection<Player> timeTargets = context.targets(config.timeTargetScope());

        attributeController.ensureApplied(speedTargets, config.movementSpeedAdd());
        worldController.tick(config, timeTargets.size(), syntheticSteps);
    }

    @Override
    public void reset(
            IMutationEffectContext context,
            Object reason,
            boolean wasActive
    ) {
        if (wasActive) {
            var targets = context.targets(config.speedTargetScope());
            broadcast(targets, config.messages().randomEnd());
            play(targets, config.endSound());
        }

        attributeController.clearAll();
        tickRateController.resetToNormal();
        worldController.onReset(config);
        activeRate = 20.0D;
        physicalTickRateApplied = false;
    }

    @Override
    public void clearPlayer(Player player) {
        attributeController.clear(player);
    }

    @Override
    public double logicalScale(IMutationEffectContext context) {
        if (physicalTickRateApplied) return 1.0D;
        return Math.max(1.0D, activeRate / 20.0D);
    }

    @Override
    public String status(IMutationEffectContext context) {
        return "rate=%s physicalTickRate=%s".formatted(
                String.format(Locale.ROOT, "%.2f", activeRate),
                physicalTickRateApplied
        );
    }

    private double randomRate(
            double min,
            double max
    ) {
        if (Double.compare(min, max) == 0) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private void broadcast(
            Collection<Player> targets,
            String message
    ) {
        var component = Component.text(message, NamedTextColor.GRAY);
        for (var player : targets) {
            player.sendMessage(component);
        }
    }

    private void play(
            Collection<Player> targets,
            AcceleratedTimeSoundConfig sound
    ) {
        for (var player : targets) {
            try {
                player.playSound(
                        player.getLocation(),
                        sound.sound(),
                        SoundCategory.PLAYERS,
                        sound.volume(),
                        sound.pitch()
                );
            } catch (IllegalArgumentException _) {
            }
        }
    }

}
