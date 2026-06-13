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
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.core.economy.store.StorePurchaseRepository;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentIds;

import java.util.ArrayList;

public final class DefaultParticleStore {

    public static final StoreId STORE_ID = StoreId.of(DefaultContentIds.key("particle_store"));

    private DefaultParticleStore() {
    }

    public static void register(ICiaExtensionContext context) {
        var storeRegistry = context.requireService(IStoreRegistry.class);
        var cosmeticRegistry = context.requireService(ICosmeticRegistry.class);
        var cosmetics = context.requireService(ICosmeticService.class);
        var wallet = context.requireService(IWalletService.class);
        var currencies = context.requireService(ICurrencyRegistry.class);
        var abilities = context.requireService(IAbilityGate.class);
        var purchases = context.getService(StorePurchaseRepository.class);
        var yml = YamlConfiguration.loadConfiguration(context.plugin()
                .getDataFolder()
                .toPath()
                .resolve("config.yml")
                .toFile());

        var title = yml.getString("game.stores.cia.particle_store.title", "粒子商店");
        int rows = yml.getInt("game.stores.cia.particle_store.rows", 6);
        storeRegistry.registerStore(context.owner(), new StoreDefinition(STORE_ID, Component.text(title), rows));

        var root = yml.getConfigurationSection("game.cosmetics.particle-trail.cosmetics.cia");
        if (root == null) return;
        for (var key : root.getKeys(false)) {
            var sec = root.getConfigurationSection(key);
            if (sec == null) continue;

            var cosmeticId = CosmeticId.of(DefaultContentIds.key(key));
            var cosmetic = cosmeticRegistry.cosmetic(cosmeticId);
            if (cosmetic == null) continue;

            boolean free = sec.getBoolean("free", false) || "none".equalsIgnoreCase(key);
            var price = price(sec.getConfigurationSection("price"));

            storeRegistry.registerItem(
                    context.owner(),
                    STORE_ID,
                    new DefaultParticleStoreItem(
                            StoreItemId.of(DefaultContentIds.key(key)),
                            cosmeticId,
                            cosmetic,
                            price,
                            wallet,
                            cosmetics,
                            currencies,
                            abilities,
                            purchases,
                            free
                    )
            );
        }
    }

    private static CurrencyCost price(ConfigurationSection sec) {
        if (sec == null) return CurrencyCost.free();
        var amounts = new ArrayList<CurrencyAmount>();

        for (var namespace : sec.getKeys(false)) {
            var nsSec = sec.getConfigurationSection(namespace);
            if (nsSec != null) {
                for (String value : nsSec.getKeys(false)) {
                    long amount = nsSec.getLong(value, 0L);
                    if (amount > 0L)
                        amounts.add(new CurrencyAmount(CurrencyId.of(CiaKey.of(CiaNamespace.parse(namespace), value)), amount));
                }
                continue;
            }
            if (sec.contains(namespace)) {
                throw new IllegalArgumentException(
                        "Currency price must use a namespaced section, for example price.cia.gunpowder"
                );
            }
        }

        return new CurrencyCost(amounts);
    }

}
