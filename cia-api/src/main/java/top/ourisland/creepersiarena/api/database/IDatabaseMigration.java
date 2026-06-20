package top.ourisland.creepersiarena.api.database;

import top.ourisland.creepersiarena.api.identity.ExtensionId;

import java.sql.Connection;

public interface IDatabaseMigration {

    ExtensionId ownerId();

    int version();

    String name();

    String checksum();

    void apply(
            Connection connection,
            DatabaseType type,
            String tablePrefix
    ) throws Exception;

}
