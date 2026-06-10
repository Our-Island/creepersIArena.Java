package top.ourisland.creepersiarena.defaultcontent.economy.store;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.economy.store.IStoreService;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.economy.store.StoreItemId;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DefaultParticleStoreAccessListener implements Listener {

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
        var root = yml.getConfigurationSection("game.abilities.cia-default-content.particle-store.settings.sources");
        if (root == null) return List.of();

        root.getKeys(false)
                .stream()
                .map(root::getConfigurationSection)
                .filter(sec -> sec != null && "block".equalsIgnoreCase(sec.getString("type", "")))
                .forEach(sec -> {
                    var world = sec.getString("world", "world");
                    var loc = sec.getList("location", List.of(0, 0, 0));
                    double radius = Math.max(0.0D, sec.getDouble("radius", 1.5D));
                    var storeId = id(sec.getString("store"), DefaultParticleStore.STORE_ID);
                    var itemId = itemId(sec.getString("item"));

                    out.add(new BlockSource(
                            world,
                            number(loc, 0),
                            number(loc, 1),
                            number(loc, 2),
                            radius,
                            storeId,
                            itemId
                    ));
                });

        return List.copyOf(out);
    }

    private static StoreId id(
            String raw,
            StoreId fallback
    ) {
        if (raw == null || raw.isBlank()) return fallback;
        String text = raw.trim();
        return text.indexOf(':') >= 0
                ? StoreId.of(text)
                : StoreId.of("cia-default-content", text);
    }

    private static StoreItemId itemId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String text = raw.trim();
        return text.indexOf(':') >= 0
                ? StoreItemId.of(text)
                : StoreItemId.of("cia-default-content", text);
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
            if (source.itemId() == null) {
                stores.openStore(player, source.storeId());
            } else {
                stores.openItem(player, source.storeId(), source.itemId());
            }
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

            var world = Bukkit.getWorld(worldName == null ? "" : worldName);
            if (world == null || !world.equals(location.getWorld())) return false;

            var center = new Location(world, x, y, z);
            return center.distanceSquared(location) <= radius * radius;
        }

    }

}
