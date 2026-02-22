package top.ourisland.creepersiarena.config.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.*;

public record GlobalConfig(
        String lang,
        Set<String> disabledJobs,
        Map<String, Lobby> lobbies,
        Game game,
        Ui ui
) {

    public static GlobalConfig defaults() {
        return new GlobalConfig(
                "en_us",
                Set.of(),
                Map.of(),
                Game.defaults(),
                Ui.defaults()
        );
    }

    public static GlobalConfig fromYaml(YamlConfiguration yml) {
        String lang = yml.getString("lang", "en_us");

        Set<String> disabledJobs = new HashSet<>(yml.getStringList("disabled-jobs"));

        // lobbies
        Map<String, Lobby> lobbies = new HashMap<>();
        ConfigurationSection lobbiesSec = yml.getConfigurationSection("lobbies");
        if (lobbiesSec != null) {
            for (String key : lobbiesSec.getKeys(false)) {
                ConfigurationSection sec = lobbiesSec.getConfigurationSection(key);
                if (sec == null) continue;
                Lobby lobby = Lobby.fromSection(sec);
                lobbies.put(key, lobby);
            }
        }

        // game
        Game game = Game.fromSection(yml.getConfigurationSection("game"));

        // ui
        Ui ui = Ui.fromSection(yml.getConfigurationSection("ui"));

        return new GlobalConfig(lang, disabledJobs, lobbies, game, ui);
    }

    // ---------------- sub models ----------------

    public enum JobSelectMode {
        HOTBAR,
        INVENTORY;

        public static JobSelectMode fromConfig(String s) {
            if (s == null) return HOTBAR;
            return switch (s.trim().toLowerCase(Locale.ROOT)) {
                case "inventory", "inv", "bag" -> INVENTORY;
                default -> HOTBAR;
            };
        }
    }

    public record Lobby(
            double x, double y, double z,
            double fromX, double fromZ,
            double toX, double toZ,
            @Nullable Entry entry
    ) {
        public static Lobby fromSection(ConfigurationSection sec) {
            List<?> loc = sec.getList("location", List.of(0, 100, 0));
            List<?> from = sec.getList("range.from", List.of(100, 100));
            List<?> to = sec.getList("range.to", List.of(-100, -100));

            double x = asDouble(loc, 0, 0);
            double y = asDouble(loc, 1, 100);
            double z = asDouble(loc, 2, 0);

            double fromX = asDouble(from, 0, 100);
            double fromZ = asDouble(from, 1, 100);
            double toX = asDouble(to, 0, -100);
            double toZ = asDouble(to, 1, -100);

            Entry entry = Entry.fromSection(sec.getConfigurationSection("entry"));
            return new Lobby(x, y, z, fromX, fromZ, toX, toZ, entry);
        }

        private static double asDouble(List<?> list, int idx, double def) {
            if (list == null || list.size() <= idx) return def;
            Object o = list.get(idx);
            if (o instanceof Number n) return n.doubleValue();
            try {
                return Double.parseDouble(String.valueOf(o));
            } catch (Exception ignored) {
                return def;
            }
        }

        public record Entry(
                long timeMs,
                double fromX, double fromY, double fromZ,
                double toX, double toY, double toZ
        ) {
            public static @Nullable Entry fromSection(ConfigurationSection sec) {
                if (sec == null) return null;

                long time = sec.getLong("time", 0L);
                if (time <= 0L) return null;

                List<?> from = sec.getList("from", List.of(0, 0, 0));
                List<?> to = sec.getList("to", List.of(0, 0, 0));

                double fromX = asDouble3(from, 0, 0);
                double fromY = asDouble3(from, 1, 0);
                double fromZ = asDouble3(from, 2, 0);

                double toX = asDouble3(to, 0, 0);
                double toY = asDouble3(to, 1, 0);
                double toZ = asDouble3(to, 2, 0);

                return new Entry(time, fromX, fromY, fromZ, toX, toY, toZ);
            }

            private static double asDouble3(List<?> list, int idx, double def) {
                if (list == null || list.size() <= idx) return def;
                Object o = list.get(idx);
                if (o instanceof Number n) return n.doubleValue();
                try {
                    return Double.parseDouble(String.valueOf(o));
                } catch (Exception ignored) {
                    return def;
                }
            }
        }
    }

    public record Game(
            Set<String> disabledModes,
            int leaveDelaySeconds,
            Battle battle,
            Steal steal
    ) {
        public static Game fromSection(ConfigurationSection sec) {
            if (sec == null) return defaults();

            Set<String> disabled = new HashSet<>(sec.getStringList("disabled-modes"));
            int leaveDelay = sec.getInt("leave-delay-seconds", 5);

            Battle battle = Battle.fromSection(sec.getConfigurationSection("battle"));
            Steal steal = Steal.fromSection(sec.getConfigurationSection("steal"));

            return new Game(disabled, Math.max(0, leaveDelay), battle, steal);
        }

        public static Game defaults() {
            return new Game(Set.of(), 5, Battle.defaults(), Steal.defaults());
        }

        public record Battle(
                int singleGameTimeSeconds,
                int respawnTimeSeconds,
                int maxTeam,
                boolean teamAutoBalancing,
                boolean forceBalancing
        ) {
            public static Battle fromSection(ConfigurationSection sec) {
                if (sec == null) return defaults();
                return new Battle(
                        sec.getInt("single-game-time", 600),
                        sec.getInt("respawn-time", 10),
                        sec.getInt("max-team", 4),
                        sec.getBoolean("team-auto-balancing", true),
                        sec.getBoolean("force-balancing", false)
                );
            }

            public static Battle defaults() {
                return new Battle(600, 10, 4, true, false);
            }
        }

        public record Steal(
                int minPlayerToStart,
                int prepareTimeSeconds,
                int totalRound,
                int timePerRoundSeconds
        ) {
            public static Steal fromSection(ConfigurationSection sec) {
                if (sec == null) return defaults();
                return new Steal(
                        sec.getInt("min-player-to-start", 2),
                        sec.getInt("prepare-time", 30),
                        sec.getInt("total-round", 10),
                        sec.getInt("time-per-round", 10)
                );
            }

            public static Steal defaults() {
                return new Steal(2, 30, 10, 10);
            }
        }
    }

    public record Ui(LobbyUi lobby) {
        public static Ui fromSection(ConfigurationSection sec) {
            if (sec == null) return defaults();
            return new Ui(LobbyUi.fromSection(sec.getConfigurationSection("lobby")));
        }

        public static Ui defaults() {
            return new Ui(LobbyUi.defaults());
        }
    }

    public record LobbyUi(JobSelectMode jobSelectMode, int jobsPerPage) {
        public static LobbyUi fromSection(ConfigurationSection sec) {
            if (sec == null) return defaults();
            String mode = sec.getString("job-select-mode", "hotbar");
            int perPage = sec.getInt("jobs-per-page", 5);
            return new LobbyUi(JobSelectMode.fromConfig(mode), perPage);
        }

        public static LobbyUi defaults() {
            return new LobbyUi(JobSelectMode.HOTBAR, 5);
        }
    }

    // -------- 通用坐标/范围 --------

    public record Vec3(int x, int y, int z) {
        public static Vec3 fromList(List<?> list) {
            if (list == null || list.size() < 3) return new Vec3(0, 0, 0);
            return new Vec3(
                    toInt(list.get(0)),
                    toInt(list.get(1)),
                    toInt(list.get(2))
            );
        }

        private static int toInt(Object o) {
            if (o instanceof Number n) return n.intValue();
            try {
                return Integer.parseInt(String.valueOf(o));
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public record Range2D(int minX, int maxX, int minZ, int maxZ) {
        public static Range2D fromSection(ConfigurationSection sec) {
            if (sec == null) return new Range2D(0, 0, 0, 0);
            List<?> from = sec.getList("from");
            List<?> to = sec.getList("to");
            int fx = (from != null && from.size() >= 2) ? Vec3.toInt(from.get(0)) : 0;
            int fz = (from != null && from.size() >= 2) ? Vec3.toInt(from.get(1)) : 0;
            int tx = (to != null && to.size() >= 2) ? Vec3.toInt(to.get(0)) : 0;
            int tz = (to != null && to.size() >= 2) ? Vec3.toInt(to.get(1)) : 0;
            return new Range2D(Math.min(fx, tx), Math.max(fx, tx), Math.min(fz, tz), Math.max(fz, tz));
        }

        public boolean contains(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }
}
