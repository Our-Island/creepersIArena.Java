package top.ourisland.creepersiarena.core.database;

import lombok.NonNull;
import top.ourisland.creepersiarena.api.database.IDatabaseMigration;
import top.ourisland.creepersiarena.api.database.IDatabaseMigrationRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DatabaseMigrationRegistry implements IDatabaseMigrationRegistry {

    private final CopyOnWriteArrayList<IDatabaseMigration> migrations = new CopyOnWriteArrayList<>();

    @Override
    public void registerMigration(
            String ownerId,
            @lombok.NonNull IDatabaseMigration migration
    ) {
        String expectedOwner = normalize(ownerId == null || ownerId.isBlank() ? migration.ownerId() : ownerId);
        String migrationOwner = normalize(migration.ownerId());

        if (!expectedOwner.equals(migrationOwner)) {
            throw new IllegalArgumentException("Migration owner mismatch: expected " + expectedOwner + " but got " + migrationOwner);
        }

        migrations.stream()
                .filter(existing ->
                        normalize(existing.ownerId()).equals(migrationOwner)
                                && existing.version() == migration.version()
                )
                .forEach(_ -> {
                    throw new IllegalArgumentException("Duplicate migration " + migrationOwner + ":" + migration.version());
                });
        migrations.add(migration);
    }

    @Override
    public @NonNull List<IDatabaseMigration> migrations() {
        var out = new ArrayList<>(migrations);
        out.sort(Comparator.comparing((IDatabaseMigration migration) -> normalize(migration.ownerId()))
                .thenComparingInt(IDatabaseMigration::version));
        return List.copyOf(out);
    }

    private static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "unknown";
        return raw.trim().toLowerCase(Locale.ROOT);
    }

}
