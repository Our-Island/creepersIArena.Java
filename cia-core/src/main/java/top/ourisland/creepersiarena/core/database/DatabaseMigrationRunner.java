package top.ourisland.creepersiarena.core.database;

import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.database.IDatabaseMigration;
import top.ourisland.creepersiarena.core.config.model.GlobalConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Locale;

public final class DatabaseMigrationRunner {

    private final JdbcDatabaseService database;
    private final DatabaseMigrationRegistry registry;
    private final GlobalConfig.Database config;
    private final Logger logger;

    public DatabaseMigrationRunner(
            JdbcDatabaseService database,
            DatabaseMigrationRegistry registry,
            GlobalConfig.Database config,
            Logger logger
    ) {
        this.database = database;
        this.registry = registry;
        this.config = config;
        this.logger = logger;
    }

    public void runAll() {
        database.runBlocking(() -> {
            try (var connection = database.connection()) {
                ensureMigrationTable(connection);
                for (IDatabaseMigration migration : registry.migrations()) {
                    runOne(connection, migration);
                }
            }
        });
        database.ready(true);

        logger.info(
                "[Database] Migrations complete: type={} prefix={} count={}",
                database.type(),
                database.tablePrefix(),
                registry.migrations().size()
        );
    }

    private void ensureMigrationTable(Connection connection) throws SQLException {
        String table = database.names().schemaMigrations();

        if (tableExists(connection, table) && !columnExists(connection, table, "owner_id")) {
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE " + table);
            }
        }

        try (var statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + table + " (" +
                            "owner_id VARCHAR(128) NOT NULL, " +
                            "version INTEGER NOT NULL, " +
                            "name VARCHAR(128) NOT NULL, " +
                            "checksum VARCHAR(128) NOT NULL, " +
                            "applied_at BIGINT NOT NULL, " +
                            "PRIMARY KEY (owner_id, version)" +
                            ")"
            );
        }
    }

    private void runOne(
            Connection connection,
            IDatabaseMigration migration
    ) throws Exception {
        String owner = normalize(migration.ownerId());
        MigrationRecord record = find(connection, owner, migration.version());

        if (record != null) {
            if (config.validateMigrationChecksum() && !record.checksum().equals(migration.checksum())) {
                throw new IllegalStateException("Migration checksum mismatch for " + owner + ":" + migration.version()
                        + " stored=" + record.checksum() + " current=" + migration.checksum());
            }
            return;
        }

        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            migration.apply(connection, database.type(), database.tablePrefix());
            insertRecord(connection, owner, migration);
            connection.commit();
            logger.info("[Database] Applied migration {}:{} {}", owner, migration.version(), migration.name());
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollback) {
                e.addSuppressed(rollback);
            }
            if (config.failOnMigrationError()) throw e;
            logger.warn("[Database] Migration failed but fail-on-error=false: {}:{} {}", owner, migration.version(), e.getMessage(), e);
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private boolean tableExists(
            Connection connection,
            String table
    ) throws SQLException {
        var meta = connection.getMetaData();
        try (var rs = meta.getTables(null, null, table, null)) {
            if (rs.next()) return true;
        }
        try (var rs = meta.getTables(null, null, table.toUpperCase(Locale.ROOT), null)) {
            return rs.next();
        }
    }

    private boolean columnExists(
            Connection connection,
            String table,
            String column
    ) throws SQLException {
        var meta = connection.getMetaData();
        try (var rs = meta.getColumns(null, null, table, column)) {
            if (rs.next()) return true;
        }
        try (var rs = meta.getColumns(null, null, table.toUpperCase(Locale.ROOT), column.toUpperCase(Locale.ROOT))) {
            return rs.next();
        }
    }

    private String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "unknown";
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    private MigrationRecord find(
            Connection connection,
            String owner,
            int version
    ) throws SQLException {
        String table = database.names().schemaMigrations();

        try (var st = connection.prepareStatement("SELECT checksum FROM " + table + " WHERE owner_id = ? AND version = ?")) {
            st.setString(1, owner);
            st.setInt(2, version);

            try (var rs = st.executeQuery()) {
                if (!rs.next()) return null;
                return new MigrationRecord(rs.getString(1));
            }
        }
    }

    private void insertRecord(
            Connection connection,
            String owner,
            IDatabaseMigration migration
    ) throws SQLException {
        String table = database.names().schemaMigrations();

        try (var st = connection.prepareStatement("INSERT INTO " + table + " (owner_id, version, name, checksum, applied_at) VALUES (?, ?, ?, ?, ?)")) {
            st.setString(1, owner);
            st.setInt(2, migration.version());
            st.setString(3, migration.name());
            st.setString(4, migration.checksum());
            st.setLong(5, Instant.now().toEpochMilli());
            st.executeUpdate();
        }
    }

    private record MigrationRecord(String checksum) {

    }

}
