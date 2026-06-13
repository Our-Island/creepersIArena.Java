package top.ourisland.creepersiarena.defaultcontent.economy.cosmetic.particle;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.IParticleCosmetic;
import top.ourisland.creepersiarena.api.economy.cosmetic.ParticleCosmeticContext;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentIds;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ParticlePreviewDisplayService {

    private static final String DISPLAYS_PATH =
            "game.abilities.cia.particle_preview_displays.settings.displays";

    private final Plugin plugin;
    private final IAbilityGate abilities;
    private final ICosmeticRegistry cosmetics;
    private final List<Display> displays;
    private final Random random = new Random();
    private ScheduledTask task;
    private long currentTick;

    public ParticlePreviewDisplayService(
            Plugin plugin,
            IAbilityGate abilities,
            ICosmeticRegistry cosmetics,
            Path configPath
    ) {
        this.plugin = plugin;
        this.abilities = abilities;
        this.cosmetics = cosmetics;
        this.displays = load(configPath);
    }

    private List<Display> load(Path configPath) {
        var out = new ArrayList<Display>();
        var yml = YamlConfiguration.loadConfiguration(configPath.toFile());
        var root = StrictConfig.section(yml, DISPLAYS_PATH, DISPLAYS_PATH);
        if (root == null) return List.of();

        for (var key : root.getKeys(false)) {
            var path = DISPLAYS_PATH + "." + key;
            var section = StrictConfig.section(root, key, path);
            if (section == null) throw new IllegalArgumentException("Missing display section at " + path);

            var cosmetic = StrictConfig.string(section, "cosmetic", null, path + ".cosmetic");
            var world = StrictConfig.string(section, "world", "world", path + ".world");
            if (world.isBlank())
                throw new IllegalArgumentException("Invalid value at " + path + ".world: expected non-blank string");
            var location = StrictConfig.list(section, "location", List.of(0, 100, 0), path + ".location");
            double[] coordinates = coordinates(location, path + ".location");
            int intervalTicks = StrictConfig.integer(section, "interval-ticks", 20, path + ".interval-ticks");
            if (intervalTicks <= 0)
                throw new IllegalArgumentException("Invalid value at " + path + ".interval-ticks: expected > 0");
            double viewerRadius = StrictConfig.decimal(section, "viewer-radius", 15.0D, path + ".viewer-radius");
            if (!(viewerRadius > 0.0D))
                throw new IllegalArgumentException("Invalid value at " + path + ".viewer-radius: expected > 0");

            out.add(new Display(
                    cosmetic == null ? CosmeticId.of(DefaultContentIds.key(key)) : CosmeticId.parse(cosmetic),
                    world,
                    coordinates[0],
                    coordinates[1],
                    coordinates[2],
                    intervalTicks,
                    viewerRadius
            ));
        }

        return List.copyOf(out);
    }

    private static double[] coordinates(List<?> values, String path) {
        if (values.size() != 3) {
            throw new IllegalArgumentException("Invalid value at " + path + ": expected exactly 3 numbers");
        }
        return new double[]{number(values.get(0), path + "[0]"), number(values.get(1), path + "[1]"), number(values.get(2), path + "[2]")};
    }

    private static double number(Object value, String path) {
        if (value instanceof Number number && Double.isFinite(number.doubleValue())) return number.doubleValue();
        throw new IllegalArgumentException("Invalid value at " + path + ": expected finite number, got " + value);
    }

    public void start() {
        stop();
        task = Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, _ -> tick(), 1L, 1L);
    }

    public void stop() {
        if (task == null) return;
        try {
            task.cancel();
        } catch (Throwable _) {
        }
        task = null;
    }

    private void tick() {
        currentTick++;
        if (!abilities.isEnabledForGame(DefaultContentAbilities.PARTICLE_PREVIEW_DISPLAYS, "particle_preview")) return;

        for (var display : displays) {
            var cosmetic = cosmetics.cosmetic(display.cosmeticId());
            if (!(cosmetic instanceof IParticleCosmetic particle)) continue;
            if (currentTick % display.intervalTicks() != 0L) continue;

            var world = Bukkit.getWorld(display.worldName());
            if (world == null) continue;

            var origin = new Location(world, display.x(), display.y(), display.z());
            var viewers = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getWorld().equals(world))
                    .filter(player -> player.getLocation().distanceSquared(origin)
                            <= display.viewerRadius() * display.viewerRadius())
                    .map(Player.class::cast)
                    .toList();

            particle.spawn(new ParticleCosmeticContext(null, origin, viewers, random, currentTick));
        }
    }

    private record Display(
            CosmeticId cosmeticId,
            String worldName,
            double x,
            double y,
            double z,
            int intervalTicks,
            double viewerRadius
    ) {

    }

}
