package top.ourisland.creepersiarena.api.database;

import org.jspecify.annotations.NonNull;

import java.util.Locale;

public enum DatabaseType {

    MYSQL,
    POSTGRESQL,
    SQLITE,
    H2;

    public static @NonNull DatabaseType parse(String raw) {
        if (raw == null) return SQLITE;
        if (raw.isBlank()) {
            throw new IllegalArgumentException("Database type must not be blank");
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid database type: expected MYSQL, POSTGRESQL, SQLITE or H2, got " + raw,
                    exception
            );
        }
    }

}
