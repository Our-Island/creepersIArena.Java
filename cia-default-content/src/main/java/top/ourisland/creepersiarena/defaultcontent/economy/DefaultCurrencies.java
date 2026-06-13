package top.ourisland.creepersiarena.defaultcontent.economy;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrency;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentIds;

public final class DefaultCurrencies {

    public static final CurrencyId
            GUNPOWDER = CurrencyId.of(DefaultContentIds.key("gunpowder")),
            TNT = CurrencyId.of(DefaultContentIds.key("tnt"));
    private static final String ROOT_PATH = "game.economy.currencies.cia";

    private DefaultCurrencies() {
    }

    public static void register(@NonNull ICiaExtensionContext context) {
        var registry = context.requireService(ICurrencyRegistry.class);
        var yml = YamlConfiguration.loadConfiguration(context.plugin()
                .getDataFolder()
                .toPath()
                .resolve("config.yml")
                .toFile());
        var root = StrictConfig.section(yml, ROOT_PATH, ROOT_PATH);

        registerCurrency(context, registry, root, "gunpowder", GUNPOWDER, "火药", Material.GUNPOWDER);
        registerCurrency(context, registry, root, "tnt", TNT, "TNT", Material.TNT);
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
        String path = ROOT_PATH + "." + key;
        var section = StrictConfig.section(root, key, path);
        var displayName = StrictConfig.string(section, "display-name", fallbackName, path + ".display-name");
        var icon = material(
                StrictConfig.string(section, "icon", null, path + ".icon"),
                fallbackIcon,
                path + ".icon"
        );

        registry.registerCurrency(
                context.owner(),
                new ConfiguredCurrency(id, Component.text(displayName), new ItemStack(icon))
        );
    }

    private static Material material(String raw, Material fallback, String path) {
        if (raw == null) return fallback;
        if (raw.isBlank())
            throw new IllegalArgumentException("Invalid value at " + path + ": material id cannot be blank");

        var material = Material.matchMaterial(raw, false);
        if (material == null) {
            throw new IllegalArgumentException("Invalid value at " + path + ": unknown material id " + raw);
        }
        return material;
    }

    private record ConfiguredCurrency(
            CurrencyId id,
            Component displayName,
            ItemStack icon
    ) implements ICurrency {

    }

}
