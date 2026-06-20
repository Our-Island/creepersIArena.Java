package top.ourisland.creepersiarena.core.database;

import lombok.NonNull;
import top.ourisland.creepersiarena.api.database.IDatabaseMigration;
import top.ourisland.creepersiarena.api.database.IDatabaseMigrationRegistry;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DatabaseMigrationRegistry implements IDatabaseMigrationRegistry {

    private final CopyOnWriteArrayList<IDatabaseMigration> migrations = new CopyOnWriteArrayList<>();

    @Override
    public void registerMigration(
            RegistrationOwner owner,
            @NonNull IDatabaseMigration migration
    ) {
        if (!owner.extensionId().equals(migration.ownerId())) {
            throw new IllegalArgumentException("Migration owner mismatch: expected %s but got %s".formatted(
                    owner.extensionId(),
                    migration.ownerId()
            ));
        }
        boolean duplicate = migrations.stream().anyMatch(existing ->
                existing.ownerId().equals(migration.ownerId()) && existing.version() == migration.version()
        );
        if (duplicate) {
            throw new IllegalArgumentException(
                    "Duplicate migration " + migration.ownerId().value() + ":" + migration.version()
            );
        }
        migrations.add(migration);
    }

    @Override
    public @NonNull List<IDatabaseMigration> migrations() {
        var out = new ArrayList<>(migrations);
        out.sort(
                Comparator.comparing((IDatabaseMigration migration) -> migration.ownerId().value())
                        .thenComparingInt(IDatabaseMigration::version)
        );
        return List.copyOf(out);
    }

    public void clearOwner(RegistrationOwner owner) {
        migrations.removeIf(migration -> migration.ownerId().equals(owner.extensionId()));
    }

}
