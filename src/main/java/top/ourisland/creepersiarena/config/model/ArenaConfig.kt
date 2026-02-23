package top.ourisland.creepersiarena.config.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

import static top.ourisland.creepersiarena.config.model.GlobalConfig.Range2D;
import static top.ourisland.creepersiarena.config.model.GlobalConfig.Vec3;

public record ArenaConfig(
        Map<String, ArenaDef> arenas
) {
    public static ArenaConfig fromYaml(YamlConfiguration yml) {
        ConfigurationSection root = yml.getConfigurationSection("arena");
        if (root == null) return empty();

        Map<String, ArenaDef> map = new LinkedHashMap<>();
        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;
            map.put(id, ArenaDef.fromSection(id, sec));
        }
        return new ArenaConfig(Collections.unmodifiableMap(map));
    }

    public static ArenaConfig empty() {
        return new ArenaConfig(Map.of());
    }

    public ArenaDef get(String id) {
        return arenas.get(id);
    }

    public record ArenaDef(
            String id,
            String nameKey,
            String type,
            Vec3 location,
            Range2D range,
            List<Vec3> spawnpoints,
            Map<String, Vec3> teamSpawnpoints,
            List<Object> redstoneBlocksRaw
    ) {
        static ArenaDef fromSection(String id, ConfigurationSection sec) {
            String nameKey = sec.getString("name", "cia.arena." + id);
            String type = sec.getString("type", "battle");

            Vec3 loc = Vec3.fromList(sec.getList("location"));
            Range2D range = Range2D.fromSection(sec.getConfigurationSection("range"));

            Object spObj = sec.get("spawnpoint");

            List<Vec3> listSpawn = new ArrayList<>();
            Map<String, Vec3> teamSpawn = new LinkedHashMap<>();

            if (spObj instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof List<?> l2) listSpawn.add(Vec3.fromList(l2));
                }
            } else if (spObj instanceof ConfigurationSection spSec) {
                for (String k : spSec.getKeys(false)) {
                    teamSpawn.put(k, Vec3.fromList(spSec.getList(k)));
                }
            } else if (spObj instanceof Map<?, ?> m) {
                for (var e : m.entrySet()) {
                    teamSpawn.put(String.valueOf(e.getKey()), Vec3.fromList((List<?>) e.getValue()));
                }
            }

            List<Object> redstoneRaw = new ArrayList<>();
            Object rs = sec.get("redstone-blocks");
            if (rs instanceof List<?> rsl) redstoneRaw.addAll(rsl);

            return new ArenaDef(
                    id, nameKey, type, loc, range,
                    Collections.unmodifiableList(listSpawn),
                    Collections.unmodifiableMap(teamSpawn),
                    Collections.unmodifiableList(redstoneRaw)
            );
        }
    }
}
