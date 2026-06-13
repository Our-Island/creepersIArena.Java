package top.ourisland.creepersiarena.defaultcontent.economy.cosmetic.particle;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.ParticleSchedule;
import top.ourisland.creepersiarena.core.economy.cosmetic.CosmeticService;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public final class DefaultParticleCosmetics {

    public static final CosmeticId NONE = CosmeticId.of(DefaultContentIds.key("none"));
    private static final String TRAIL_PATH = "game.cosmetics.particle-trail";
    private static final String COSMETICS_PATH = TRAIL_PATH + ".cosmetics.cia";

    private DefaultParticleCosmetics() {
    }

    public static void register(ICiaExtensionContext context) {
        var registry = context.requireService(ICosmeticRegistry.class);
        var gate = context.requireService(IAbilityGate.class);
        var yml = YamlConfiguration.loadConfiguration(
                context.plugin().getDataFolder().toPath().resolve("config.yml").toFile()
        );

        var trail = StrictConfig.section(yml, TRAIL_PATH, TRAIL_PATH);
        var viewerRadius = StrictConfig.decimal(trail, "viewer-radius", 15.0D, TRAIL_PATH + ".viewer-radius");
        requirePositive(viewerRadius, TRAIL_PATH + ".viewer-radius");

        var runtime = context.getService(CosmeticService.class);
        if (runtime != null) runtime.viewerRadius(viewerRadius);

        var cosmeticsRoot = StrictConfig.section(trail, "cosmetics", TRAIL_PATH + ".cosmetics");
        var root = StrictConfig.section(cosmeticsRoot, "cia", COSMETICS_PATH);
        if (root == null) return;

        for (var key : root.getKeys(false)) {
            String path = COSMETICS_PATH + "." + key;
            var section = StrictConfig.section(root, key, path);
            if (section == null) {
                throw new IllegalArgumentException("Missing cosmetic section at " + path);
            }
            registry.registerCosmetic(context.owner(), fromSection(key, section, gate, path));
        }
    }

    private static ConfiguredParticleCosmetic fromSection(
            String key,
            ConfigurationSection section,
            IAbilityGate gate,
            String path
    ) {
        var id = CosmeticId.of(DefaultContentIds.key(key));
        var name = StrictConfig.string(section, "display-name", key, path + ".display-name");
        var material = material(
                StrictConfig.string(section, "icon", null, path + ".icon"),
                Material.FIREWORK_STAR,
                path + ".icon"
        );
        int interval = StrictConfig.integer(section, "interval-ticks", 20, path + ".interval-ticks");
        if (interval <= 0)
            throw new IllegalArgumentException("Invalid value at " + path + ".interval-ticks: expected > 0");

        var rawEmissions = StrictConfig.list(section, "emissions", List.of(), path + ".emissions");
        var emissions = new ArrayList<ParticleEmission>(rawEmissions.size());
        for (int index = 0; index < rawEmissions.size(); index++) {
            Object raw = rawEmissions.get(index);
            if (!(raw instanceof Map<?, ?> map)) {
                throw invalid(path + ".emissions[" + index + "]", "mapping", raw);
            }
            emissions.add(emission(map, path + ".emissions[" + index + "]"));
        }

        return new ConfiguredParticleCosmetic(
                id,
                Component.text(name),
                new ItemStack(material),
                new ParticleSchedule(interval),
                List.copyOf(emissions),
                gate
        );
    }

    private static Material material(
            String raw,
            Material fallback,
            String path
    ) {
        if (raw == null) return fallback;
        if (raw.isBlank())
            throw new IllegalArgumentException("Invalid value at " + path + ": material id cannot be blank");
        var material = Material.matchMaterial(raw, false);
        if (material == null)
            throw new IllegalArgumentException("Invalid value at " + path + ": unknown material id " + raw);
        return material;
    }

    private static ParticleEmission emission(Map<?, ?> map, String path) {
        var particleId = requiredString(map, "particle", path + ".particle");
        var relative = vector(map, "relative-location", path + ".relative-location", 0.0D, 0.0D, 0.0D);
        var offset = vector(map, "offset", path + ".offset", 0.0D, 0.0D, 0.0D);
        var dust = vector(map, "dust-color", path + ".dust-color", 0.0D, 0.0D, 0.0D);
        IntStream.range(0, dust.length)
                .filter(index -> dust[index] < 0.0D || dust[index] > 1.0D)
                .forEach(index -> {
                    throw new IllegalArgumentException("Invalid value at " + path + ".dust-color[" + index + "]: expected 0.0..1.0");
                });

        double speed = optionalNumber(map, "speed", 0.0D, path + ".speed");
        if (speed < 0.0D)
            throw new IllegalArgumentException("Invalid value at " + path + ".speed: expected >= 0");

        int count = optionalInteger(map, "count", 1, path + ".count");
        if (count <= 0)
            throw new IllegalArgumentException("Invalid value at " + path + ".count: expected > 0");

        float dustScale = (float) optionalNumber(map, "dust-scale", 1.0D, path + ".dust-scale");
        if (!(dustScale > 0.0F))
            throw new IllegalArgumentException("Invalid value at " + path + ".dust-scale: expected > 0");

        return new ParticleEmission(
                ParticleEmission.particle(particleId),
                relative[0], relative[1], relative[2],
                offset[0], offset[1], offset[2],
                speed,
                count,
                optionalBoolean(map, "force", true, path + ".force"),
                optionalBoolean(map, "random-color", false, path + ".random-color"),
                Color.fromRGB(color(dust[0]), color(dust[1]), color(dust[2])),
                dustScale
        );
    }

    private static double[] vector(
            Map<?, ?> map,
            String key,
            String path,
            double defaultX,
            double defaultY,
            double defaultZ
    ) {
        Object raw = map.get(key);
        if (raw == null) return new double[]{defaultX, defaultY, defaultZ};
        if (!(raw instanceof List<?> list)) throw invalid(path, "three-number list", raw);
        if (list.size() != 3)
            throw new IllegalArgumentException("Invalid value at " + path + ": expected exactly 3 numbers");
        return new double[]{number(list.get(0), path + "[0]"), number(list.get(1), path + "[1]"), number(list.get(2), path + "[2]")};
    }

    private static String requiredString(
            Map<?, ?> map,
            String key,
            String path
    ) {
        Object raw = map.get(key);
        if (!(raw instanceof String text) || text.isBlank()) throw invalid(path, "non-blank string", raw);
        return text;
    }

    private static double optionalNumber(
            Map<?, ?> map,
            String key,
            double fallback,
            String path
    ) {
        Object raw = map.get(key);
        return raw == null ? fallback : number(raw, path);
    }

    private static int optionalInteger(
            Map<?, ?> map,
            String key,
            int fallback,
            String path
    ) {
        Object raw = map.get(key);
        if (raw == null) return fallback;
        if (raw instanceof Byte || raw instanceof Short || raw instanceof Integer) return ((Number) raw).intValue();
        if (raw instanceof Long value && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)
            return value.intValue();
        throw invalid(path, "integer", raw);
    }

    private static boolean optionalBoolean(
            Map<?, ?> map,
            String key,
            boolean fallback,
            String path
    ) {
        Object raw = map.get(key);
        if (raw == null) return fallback;
        if (raw instanceof Boolean value) return value;
        throw invalid(path, "boolean", raw);
    }

    private static double number(Object raw, String path) {
        if (raw instanceof Number value && Double.isFinite(value.doubleValue())) return value.doubleValue();
        throw invalid(path, "finite number", raw);
    }

    private static int color(double component) {
        return (int) Math.round(component * 255.0D);
    }

    private static void requirePositive(double value, String path) {
        if (!(value > 0.0D)) throw new IllegalArgumentException("Invalid value at " + path + ": expected > 0");
    }

    private static IllegalArgumentException invalid(
            String path,
            String expected,
            Object raw
    ) {
        String actual = raw == null ? "null" : raw.getClass().getSimpleName() + " (" + raw + ")";
        return new IllegalArgumentException("Invalid value at " + path + ": expected " + expected + ", got " + actual);
    }

}
