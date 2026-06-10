package top.ourisland.creepersiarena.defaultcontent.economy;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrency;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;

public final class DefaultCurrencies {

    public static final CurrencyId
            GUNPOWDER = CurrencyId.of("cia-default-content:gunpowder"),
            TNT = CurrencyId.of("cia-default-content:tnt");

    private DefaultCurrencies() {
    }

    public static void register(@NonNull ICiaExtensionContext context) {
        var registry = context.requireService(ICurrencyRegistry.class);
        var yml = YamlConfiguration.loadConfiguration(context.plugin()
                .getDataFolder()
                .toPath()
                .resolve("config.yml")
                .toFile());
        var root = yml.getConfigurationSection("game.economy.currencies");

        registerCurrency(
                context,
                registry,
                root,
                "gunpowder",
                GUNPOWDER,
                "火药",
                Material.GUNPOWDER
        );
        registerCurrency(
                context,
                registry,
                root,
                "tnt",
                TNT,
                "TNT",
                Material.TNT
        );
    }

    private static void registerCurrency(
            ICiaExtensionContext context,
            ICurrencyRegistry registry,
            ConfigurationSection root,
            String key,
            CurrencyId id,
            String fallbackName,
            Material fallbackIcon
    ) {
        var sec = root == null
                ? null
                : root.getConfigurationSection(key);
        var displayName = sec == null
                ? fallbackName
                : sec.getString("display-name", fallbackName);
        var material = material(
                sec == null
                        ? null
                        : sec.getString("icon"),
                fallbackIcon
        );

        registry.registerCurrency(
                context.extensionId(),
                new ConfiguredCurrency(id, Component.text(displayName), new ItemStack(material))
        );
    }

    private static Material material(
            String raw,
            Material fallback
    ) {
        if (raw == null || raw.isBlank()) return fallback;

        String name = raw.trim();
        int colon = name.indexOf(':');
        if (colon >= 0) name = name.substring(colon + 1);

        var material = Material.matchMaterial(name);
        return material == null ? fallback : material;
    }

    private record ConfiguredCurrency(
            CurrencyId id,
            Component displayName,
            ItemStack icon
    ) implements ICurrency {

    }

}
