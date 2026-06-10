package top.ourisland.creepersiarena.api.database;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public interface IDatabaseService {

    @NonNull DatabaseType type();

    boolean ready();

    @NonNull String tablePrefix();

    <T> CompletableFuture<T> read(IDatabaseWork<T> work);

    <T> CompletableFuture<T> write(IDatabaseWork<T> work);

    <T> CompletableFuture<T> transaction(IDatabaseWork<T> work);

}
