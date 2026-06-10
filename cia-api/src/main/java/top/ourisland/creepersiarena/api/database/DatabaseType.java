package top.ourisland.creepersiarena.api.database;

import org.jspecify.annotations.NonNull;

import java.util.Locale;

public enum DatabaseType {

    MYSQL,
    POSTGRESQL,
    SQLITE,
    H2;

    public static @NonNull DatabaseType parse(String raw) {
        if (raw == null || raw.isBlank()) return SQLITE;
        return switch (raw.trim().toLowerCase(Locale.ROOT).replace('-', '_')) {
            case "mysql", "mariadb" -> MYSQL;
            case "postgres", "postgresql", "pgsql" -> POSTGRESQL;
            case "h2" -> H2;
            default -> SQLITE;
        };
    }

}
