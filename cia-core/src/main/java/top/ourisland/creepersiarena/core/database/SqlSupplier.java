package top.ourisland.creepersiarena.core.database;

@FunctionalInterface
public interface SqlSupplier<T> {

    T get() throws Exception;

}
