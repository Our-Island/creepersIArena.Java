package top.ourisland.creepersiarena.core.database;

import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class DatabaseSchemaUtils {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");

    private DatabaseSchemaUtils() {
    }

    static void createIndex(
            @NonNull Connection connection,
            String table,
            String name,
            String columns
    ) throws SQLException {
        executeDdl(
                connection,
                "CREATE INDEX " + identifier(name)
                        + " ON " + identifier(table)
                        + " (" + identifierList(columns) + ")"
        );
    }

    static void executeDdl(
            @NonNull Connection connection,
            @Language(value = "SQL") String sql
    ) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    static @NonNull String identifier(String raw) {
        if (raw == null || !IDENTIFIER.matcher(raw).matches()) {
            throw new IllegalArgumentException("Unsafe database identifier: " + raw);
        }
        return raw;
    }

    private static String identifierList(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Index columns must not be blank");
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(DatabaseSchemaUtils::identifier)
                .collect(Collectors.joining(", "));
    }

}
