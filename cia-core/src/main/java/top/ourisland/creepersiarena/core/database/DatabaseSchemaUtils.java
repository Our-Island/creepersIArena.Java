package top.ourisland.creepersiarena.core.database;

import org.jspecify.annotations.NonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class DatabaseSchemaUtils {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");

    private DatabaseSchemaUtils() {
    }

    static boolean tableExists(
            @NonNull Connection connection,
            String table
    ) throws SQLException {
        String safeTable = identifier(table);
        var meta = connection.getMetaData();

        try (var rs = meta.getTables(null, null, safeTable, null)) {
            if (rs.next()) return true;
        }

        try (var rs = meta.getTables(null, null, safeTable.toUpperCase(Locale.ROOT), null)) {
            return rs.next();
        }
    }

    static @NonNull String identifier(String raw) {
        if (raw == null || !IDENTIFIER.matcher(raw).matches()) {
            throw new IllegalArgumentException("Unsafe database identifier: " + raw);
        }
        return raw;
    }

    static boolean columnExists(
            @NonNull Connection connection,
            String table,
            String column
    ) throws SQLException {
        String safeTable = identifier(table);
        String safeColumn = identifier(column);
        var meta = connection.getMetaData();

        try (var rs = meta.getColumns(null, null, safeTable, safeColumn)) {
            if (rs.next()) return true;
        }

        try (var rs = meta.getColumns(null, null, safeTable.toUpperCase(Locale.ROOT), safeColumn.toUpperCase(Locale.ROOT))) {
            return rs.next();
        }
    }

    static void dropTable(
            @NonNull Connection connection,
            String table
    ) throws SQLException {
        executeDdl(connection, "DROP TABLE " + identifier(table));
    }

    static void executeDdl(
            @NonNull Connection connection,
            String sql
    ) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    static void addColumn(
            @NonNull Connection connection,
            String table,
            String ddl
    ) throws SQLException {
        executeDdl(connection, "ALTER TABLE " + identifier(table) + " ADD COLUMN " + ddl);
    }

    static void createIndex(
            @NonNull Connection connection,
            String table,
            String name,
            String columns
    ) {
        String safeTable = identifier(table);
        String safeName = identifier(name);
        String safeColumns = identifierList(columns);

        try {
            executeDdl(connection, "CREATE INDEX IF NOT EXISTS " + safeName + " ON " + safeTable + " (" + safeColumns + ")");
        } catch (SQLException first) {
            try {
                executeDdl(connection, "CREATE INDEX " + safeName + " ON " + safeTable + " (" + safeColumns + ")");
            } catch (SQLException _) {
                // Index creation differences are non-fatal; table correctness is the critical migration step.
            }
        }
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
