package top.ourisland.creepersiarena.api.database;

import java.sql.Connection;

@FunctionalInterface
public interface IDatabaseWork<T> {

    T run(Connection connection) throws Exception;

}
