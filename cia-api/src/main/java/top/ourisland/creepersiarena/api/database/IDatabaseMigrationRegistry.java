package top.ourisland.creepersiarena.api.database;

import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.List;

public interface IDatabaseMigrationRegistry {

    void registerMigration(RegistrationOwner owner, IDatabaseMigration migration);

    List<IDatabaseMigration> migrations();

}
