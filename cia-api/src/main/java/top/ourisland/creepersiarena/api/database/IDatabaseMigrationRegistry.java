package top.ourisland.creepersiarena.api.database;

import java.util.List;

public interface IDatabaseMigrationRegistry {

    void registerMigration(String ownerId, IDatabaseMigration migration);

    List<IDatabaseMigration> migrations();

}
