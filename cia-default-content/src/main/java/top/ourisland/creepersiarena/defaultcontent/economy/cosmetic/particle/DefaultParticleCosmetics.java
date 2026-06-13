package top.ourisland.creepersiarena.defaultcontent.economy.cosmetic.particle;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.ParticleSchedule;
import top.ourisland.creepersiarena.core.economy.cosmetic.CosmeticService;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentIds;

import java.util.List;
import java.util.Map;

public final class DefaultParticleCosmetics {

    public static final CosmeticId NONE = CosmeticId.of(DefaultContentIds.key("none"));

    private DefaultParticleCosmetics() {
    }

    public static void register(ICiaExtensionContext context) {
        var registry = context.requireService(ICosmeticRegistry.class);
        var gate = context.requireService(IAbilityGate.class);
        var yml = YamlConfiguration.loadConfiguration(
                context.plugin()
                        .getDataFolder()
                        .toPath()
                        .resolve("config.yml")
                        .toFile()
        );

        var runtime = context.getService(CosmeticService.class);
        if (runtime != null) {
            runtime.viewerRadius(yml.getDouble("game.cosmetics.particle-trail.viewer-radius", 15.0D));
        }

        var root = yml.getConfigurationSection("game.cosmetics.particle-trail.cosmetics.cia");
        if (root == null) return;

        for (var key : root.getKeys(false)) {
            var sec = root.getConfigurationSection(key);
            if (sec == null) continue;
            var cosmetic = fromSection(key, sec, gate);
            registry.registerCosmetic(context.owner(), cosmetic);
        }
    }

    private static ConfiguredParticleCosmetic fromSection(
            String key,
            ConfigurationSection sec,
            IAbilityGate gate
    ) {
        var id = CosmeticId.of(DefaultContentIds.key(key));
        var name = sec.getString("display-name", key);
        var material = material(sec.getString("icon"), Material.FIREWORK_STAR);
        int interval = Math.max(1, sec.getInt("interval-ticks", 20));
        var emissions = sec.getMapList("emissions").stream()
                .map(DefaultParticleCosmetics::emission)
                .toList();

        return new ConfiguredParticleCosmetic(
                id,
                Component.text(name),
                new ItemStack(material),
                new ParticleSchedule(interval),
                emissions,
                gate
        );
    }

    private static Material material(
            String raw,
            Material fallback
    ) {
        if (raw == null || raw.isBlank()) return fallback;
        var material = Material.matchMaterial(raw.trim());
        return material == null ? fallback : material;
    }

    private static ParticleEmission emission(Map<?, ?> map) {
        var particle = ParticleEmission.particle(asString(map.get("particle")));
        var rel = asList(map.get("relative-location"));
        var offset = asList(map.get("offset"));
        var dust = asList(map.get("dust-color"));
        return new ParticleEmission(
                particle,
                asDouble(rel, 0, 0.0D),
                asDouble(rel, 1, 0.0D),
                asDouble(rel, 2, 0.0D),
                asDouble(offset, 0, 0.0D),
                asDouble(offset, 1, 0.0D),
                asDouble(offset, 2, 0.0D),
                asDouble(map.get("speed"), 0.0D),
                Math.max(1, (int) asDouble(map.get("count"), 1.0D)),
                asBoolean(map.get("force"), true),
                asBoolean(map.get("random-color"), false),
                Color.fromRGB(
                        clampColor(asDouble(dust, 0, 0.0D)),
                        clampColor(asDouble(dust, 1, 0.0D)),
                        clampColor(asDouble(dust, 2, 0.0D))
                ),
                (float) asDouble(map.get("dust-scale"), 1.0D)
        );
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static List<?> asList(Object value) {
        return value instanceof List<?> list ? list : List.of();
    }

    private static double asDouble(
            List<?> values,
            int index,
            double fallback
    ) {
        if (values == null || values.size() <= index) return fallback;
        return asDouble(values.get(index), fallback);
    }

    private static double asDouble(
            Object value,
            double fallback
    ) {
        if (value instanceof Number number) return number.doubleValue();
        if (value == null) return fallback;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException _) {
            return fallback;
        }
    }

    private static boolean asBoolean(
            Object value,
            boolean fallback
    ) {
        if (value instanceof Boolean b) return b;
        if (value == null) return fallback;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static int clampColor(double value) {
        if (value <= 1.0D) value = value * 255.0D;
        return Math.clamp((int) Math.round(value), 0, 255);
    }

}
