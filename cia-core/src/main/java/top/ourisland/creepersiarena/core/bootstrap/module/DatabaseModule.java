package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.api.database.IDatabaseMigrationRegistry;
import top.ourisland.creepersiarena.api.database.IDatabaseService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.database.CoreSchemaMigration;
import top.ourisland.creepersiarena.core.database.DatabaseMigrationRegistry;
import top.ourisland.creepersiarena.core.database.DatabaseMigrationRunner;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;

@CiaBootstrapModule(name = "database", order = 610)
public final class DatabaseModule implements IBootstrapModule {

    @Override
    public String name() {
        return "database";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var cfg = rt.requireService(ConfigManager.class).globalConfig().database();
            var service = new JdbcDatabaseService(rt.plugin(), rt.log(), cfg);
            var migrations = new DatabaseMigrationRegistry();
            migrations.registerMigration("core", new CoreSchemaMigration());

            rt.putService(JdbcDatabaseService.class, service);
            rt.putService(IDatabaseService.class, service);
            rt.putService(DatabaseMigrationRegistry.class, migrations);
            rt.putService(IDatabaseMigrationRegistry.class, migrations);
        }, "Connecting database...", "Database service loaded.");
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var cfg = rt.requireService(ConfigManager.class).globalConfig().database();
            var service = rt.requireService(JdbcDatabaseService.class);
            var migrations = rt.requireService(DatabaseMigrationRegistry.class);
            new DatabaseMigrationRunner(service, migrations, cfg, rt.log()).runAll();
        }, "Running database migrations...", "Database ready.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.getService(JdbcDatabaseService.class);
            if (service != null) service.shutdown();
        }, "Stopping database runtime...", "Database runtime stopped.");
    }

}
