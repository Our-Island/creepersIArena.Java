package top.ourisland.creepersiarena.api.database;

import java.sql.Connection;

public interface IDatabaseMigration {

    String ownerId();

    int version();

    String name();

    String checksum();

    void apply(Connection connection, DatabaseType type, String tablePrefix) throws Exception;

}
