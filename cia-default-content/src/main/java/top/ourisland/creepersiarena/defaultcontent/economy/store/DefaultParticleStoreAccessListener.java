package top.ourisland.creepersiarena.defaultcontent.economy.store;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.economy.store.IStoreService;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.economy.store.StoreItemId;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DefaultParticleStoreAccessListener implements Listener {

    private static final String SOURCES_PATH = "game.abilities.cia.particle_store.settings.sources";

    private final IStoreService stores;
    private final IAbilityGate abilities;
    private final Path configPath;
    private volatile List<BlockSource> blockSources;

    public DefaultParticleStoreAccessListener(
            IStoreService stores,
            IAbilityGate abilities,
            Path configPath
    ) {
        this.stores = stores;
        this.abilities = abilities;
        this.configPath = configPath;
        this.blockSources = loadSources(configPath);
    }

    private List<BlockSource> loadSources(Path configPath) {
        var out = new ArrayList<BlockSource>();
        var yml = YamlConfiguration.loadConfiguration(configPath.toFile());
        var root = StrictConfig.section(yml, SOURCES_PATH, SOURCES_PATH);
        if (root == null) return List.of();

        for (var key : root.getKeys(false)) {
            String path = SOURCES_PATH + "." + key;
            var section = StrictConfig.section(root, key, path);
            if (section == null) throw new IllegalArgumentException("Missing store source section at " + path);

            String type = StrictConfig.string(section, "type", null, path + ".type");
            if (!"block".equals(type)) {
                throw new IllegalArgumentException("Invalid value at " + path + ".type: expected block");
            }
            String world = StrictConfig.string(section, "world", "world", path + ".world");
            if (world.isBlank())
                throw new IllegalArgumentException("Invalid value at " + path + ".world: expected non-blank string");
            double[] location = coordinates(
                    StrictConfig.list(section, "location", List.of(0, 0, 0), path + ".location"),
                    path + ".location"
            );
            double radius = StrictConfig.decimal(section, "radius", 1.5D, path + ".radius");
            if (!(radius > 0.0D))
                throw new IllegalArgumentException("Invalid value at " + path + ".radius: expected > 0");

            String store = StrictConfig.string(section, "store", null, path + ".store");
            String item = StrictConfig.string(section, "item", null, path + ".item");
            out.add(new BlockSource(
                    world,
                    location[0], location[1], location[2],
                    radius,
                    store == null ? DefaultParticleStore.STORE_ID : StoreId.parse(store),
                    item == null ? null : StoreItemId.parse(item)
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

    public void reload() {
        this.blockSources = loadSources(configPath);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        var player = event.getPlayer();
        if (!abilities.isEnabled(DefaultContentAbilities.PARTICLE_STORE, player, "particle_store_source")) return;

        var clicked = event.getClickedBlock().getLocation();
        for (BlockSource source : blockSources) {
            if (!source.matches(clicked)) continue;
            event.setCancelled(true);
            if (source.itemId() == null) stores.openStore(player, source.storeId());
            else stores.openItem(player, source.storeId(), source.itemId());
            return;
        }
    }

    private record BlockSource(
            String worldName,
            double x,
            double y,
            double z,
            double radius,
            StoreId storeId,
            StoreItemId itemId
    ) {

        boolean matches(Location location) {
            if (location == null || location.getWorld() == null) return false;
            var world = Bukkit.getWorld(worldName);
            if (world == null || !world.equals(location.getWorld())) return false;
            var center = new Location(world, x, y, z);
            return center.distanceSquared(location) <= radius * radius;
        }

    }

}
