package top.ourisland.creepersiarena.defaultcontent.cosmetic.particle;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.cosmetic.IParticleCosmetic;
import top.ourisland.creepersiarena.api.cosmetic.ParticleCosmeticContext;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ParticlePreviewDisplayService {

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
        var root = yml.getConfigurationSection("game.abilities.cia-default-content.particle-preview-displays.settings.displays");

        if (root == null) return out;

        for (String key : root.getKeys(false)) {
            var sec = root.getConfigurationSection(key);
            if (sec == null) continue;

            String cosmetic = sec.getString("cosmetic", "cia-default-content:" + key);
            String world = sec.getString("world", "world");
            var loc = sec.getList("location", List.of(0, 100, 0));

            out.add(new Display(
                    CosmeticId.of(cosmetic),
                    world,
                    number(loc, 0),
                    number(loc, 1),
                    number(loc, 2),
                    Math.max(1, sec.getInt("interval-ticks", 20)),
                    Math.max(1.0D, sec.getDouble("viewer-radius", 15.0D))
            ));
        }

        return out;
    }

    private static double number(
            List<?> values,
            int index
    ) {
        if (values == null || values.size() <= index) return 0.0D;

        Object value = values.get(index);
        if (value instanceof Number number) return number.doubleValue();

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException _) {
            return 0.0D;
        }
    }

    public void start() {
        stop();

        task = Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                _ -> tick(),
                1L,
                1L
        );
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

        for (Display display : displays) {
            var cosmetic = cosmetics.cosmetic(display.cosmeticId());
            if (!(cosmetic instanceof IParticleCosmetic particle)) continue;
            if (currentTick % Math.max(1, display.intervalTicks()) != 0L) continue;

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
