package top.ourisland.creepersiarena.defaultcontent.economy.store;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.economy.*;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticService;
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.economy.store.StoreDefinition;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.economy.store.StoreItemId;

import java.util.ArrayList;

public final class DefaultParticleStore {

    public static final StoreId STORE_ID = StoreId.of("cia-default-content:particle_store");

    private DefaultParticleStore() {
    }

    public static void register(ICiaExtensionContext context) {
        var storeRegistry = context.requireService(IStoreRegistry.class);
        var cosmeticRegistry = context.requireService(ICosmeticRegistry.class);
        var cosmetics = context.requireService(ICosmeticService.class);
        var wallet = context.requireService(IWalletService.class);
        var currencies = context.requireService(ICurrencyRegistry.class);
        var abilities = context.requireService(IAbilityGate.class);
        var yml = YamlConfiguration.loadConfiguration(context.plugin()
                .getDataFolder()
                .toPath()
                .resolve("config.yml")
                .toFile());

        String title = yml.getString("game.stores.particle-store.title", "粒子商店");
        int rows = yml.getInt("game.stores.particle-store.rows", 6);
        storeRegistry.registerStore(context.extensionId(), new StoreDefinition(STORE_ID, Component.text(title), rows));

        var root = yml.getConfigurationSection("game.cosmetics.particle-trail.cosmetics");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            var sec = root.getConfigurationSection(key);
            if (sec == null) continue;

            var cosmeticId = CosmeticId.of("cia-default-content", key);
            var cosmetic = cosmeticRegistry.cosmetic(cosmeticId);
            if (cosmetic == null) continue;

            boolean free = sec.getBoolean("free", false) || "none".equalsIgnoreCase(key);
            var price = price(sec.getConfigurationSection("price"));

            storeRegistry.registerItem(
                    context.extensionId(),
                    STORE_ID,
                    new DefaultParticleStoreItem(
                            StoreItemId.of("cia-default-content", key),
                            cosmeticId,
                            cosmetic,
                            price,
                            wallet,
                            cosmetics,
                            currencies,
                            abilities,
                            free
                    )
            );
        }
    }

    private static CurrencyCost price(ConfigurationSection sec) {
        if (sec == null) return CurrencyCost.free();
        var amounts = new ArrayList<CurrencyAmount>();

        for (String namespace : sec.getKeys(false)) {
            var nsSec = sec.getConfigurationSection(namespace);
            if (nsSec != null) {
                for (String value : nsSec.getKeys(false)) {
                    long amount = nsSec.getLong(value, 0L);
                    if (amount > 0L) amounts.add(new CurrencyAmount(CurrencyId.of(namespace, value), amount));
                }
                continue;
            }
            long amount = sec.getLong(namespace, 0L);
            if (amount > 0L) amounts.add(new CurrencyAmount(CurrencyId.of("cia-default-content", namespace), amount));
        }

        return new CurrencyCost(amounts);
    }

}
