package top.ourisland.creepersiarena.core.database;

import top.ourisland.creepersiarena.api.database.DatabaseType;
import top.ourisland.creepersiarena.api.database.IDatabaseMigration;
import top.ourisland.creepersiarena.api.identity.ExtensionId;

import java.sql.Connection;

public final class CorePreferenceSchemaMigration implements IDatabaseMigration {

    @Override
    public ExtensionId ownerId() {
        return new ExtensionId("core");
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String name() {
        return "player_preferences";
    }

    @Override
    public String checksum() {
        return "core-player-preferences-v2-2026-07-01";
    }

    @Override
    public void apply(
            Connection connection,
            DatabaseType type,
            String tablePrefix
    ) throws Exception {
        var names = new DatabaseNames(tablePrefix);

        try (var statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE " + names.playerPreferences() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "particles_enabled BOOLEAN NOT NULL, " +
                            "scoreboard_enabled BOOLEAN NOT NULL, " +
                            "updated_at BIGINT NOT NULL" +
                            ")"
            );
        }
    }

}
