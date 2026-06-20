package top.ourisland.creepersiarena.defaultcontent.economy.store;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.economy.CurrencyAmount;
import top.ourisland.creepersiarena.api.economy.CurrencyCost;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.economy.IWalletService;
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

    private static final String STORE_PATH = "game.stores.cia.particle_store";
    private static final String COSMETICS_PATH = "game.cosmetics.particle-trail.cosmetics.cia";

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
        var yml = YamlConfiguration.loadConfiguration(
                context.plugin().getDataFolder().toPath().resolve("config.yml").toFile()
        );

        var storeSection = StrictConfig.section(yml, STORE_PATH, STORE_PATH);
        var title = StrictConfig.string(storeSection, "title", "粒子商店", STORE_PATH + ".title");
        int rows = StrictConfig.integer(storeSection, "rows", 6, STORE_PATH + ".rows");
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Invalid value at " + STORE_PATH + ".rows: expected 1..6");
        }
        storeRegistry.registerStore(context.owner(), new StoreDefinition(STORE_ID, Component.text(title), rows));

        var root = StrictConfig.section(yml, COSMETICS_PATH, COSMETICS_PATH);
        if (root == null) return;
        for (var key : root.getKeys(false)) {
            String path = COSMETICS_PATH + "." + key;
            var section = StrictConfig.section(root, key, path);
            if (section == null) throw new IllegalArgumentException("Missing cosmetic section at " + path);

            var cosmeticId = CosmeticId.of(DefaultContentIds.key(key));
            var cosmetic = cosmeticRegistry.cosmetic(cosmeticId);
            if (cosmetic == null) {
                throw new IllegalStateException("Store item " + path + " references an unregistered cosmetic " + cosmeticId);
            }

            boolean free = StrictConfig.bool(section, "free", false, path + ".free") || key.equals("none");
            var price = price(StrictConfig.section(section, "price", path + ".price"), path + ".price");
            if (!free && price.amounts().isEmpty()) {
                throw new IllegalArgumentException("Non-free cosmetic at " + path + " must declare a positive price");
            }

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

    private static CurrencyCost price(ConfigurationSection section, String path) {
        if (section == null) return CurrencyCost.free();
        var amounts = new ArrayList<CurrencyAmount>();

        for (var namespace : section.getKeys(false)) {
            String namespacePath = path + "." + namespace;
            var namespaceSection = StrictConfig.section(section, namespace, namespacePath);
            if (namespaceSection == null) {
                throw new IllegalArgumentException("Currency price must use namespaced sections at " + namespacePath);
            }
            var parsedNamespace = CiaNamespace.parse(namespace);
            for (String value : namespaceSection.getKeys(false)) {
                String amountPath = namespacePath + "." + value;
                long amount = StrictConfig.longValue(namespaceSection, value, 0L, amountPath);
                if (amount <= 0L) {
                    throw new IllegalArgumentException("Invalid value at " + amountPath + ": expected > 0");
                }
                amounts.add(new CurrencyAmount(CurrencyId.of(CiaKey.of(parsedNamespace, value)), amount));
            }
        }

        return new CurrencyCost(amounts);
    }

}
