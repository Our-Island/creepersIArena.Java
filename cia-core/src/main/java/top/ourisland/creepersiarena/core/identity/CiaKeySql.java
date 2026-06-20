package top.ourisland.creepersiarena.core.identity;

import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Canonical JDBC codec for CIA resource ids stored as namespace/path column pairs.
 */
public final class CiaKeySql {

    private CiaKeySql() {
    }

    public static void bindNullable(
            @lombok.NonNull PreparedStatement statement,
            int namespaceIndex,
            int pathIndex,
            CiaKey key
    ) throws SQLException {
        if (key == null) {
            statement.setString(namespaceIndex, null);
            statement.setString(pathIndex, null);
            return;
        }
        bind(statement, namespaceIndex, pathIndex, key);
    }

    public static void bind(
            @lombok.NonNull PreparedStatement statement,
            int namespaceIndex,
            int pathIndex,
            @lombok.NonNull CiaKey key
    ) throws SQLException {
        statement.setString(namespaceIndex, key.namespace().value());
        statement.setString(pathIndex, key.path().value());
    }

    public static CiaKey read(
            @lombok.NonNull ResultSet result,
            String namespaceColumn,
            String pathColumn
    ) throws SQLException {
        return read(result.getString(namespaceColumn), result.getString(pathColumn));
    }

    public static CiaKey read(String namespace, String path) {
        if (namespace == null || path == null) {
            throw new IllegalArgumentException("CIA id namespace/path columns must both be non-null");
        }
        return CiaKey.of(CiaNamespace.parse(namespace), path);
    }

    public static CiaKey read(
            @lombok.NonNull ResultSet result,
            int namespaceIndex,
            int pathIndex
    ) throws SQLException {
        return read(result.getString(namespaceIndex), result.getString(pathIndex));
    }

    public static CiaKey readNullable(String namespace, String path) {
        if (namespace == null && path == null) return null;
        if (namespace == null || path == null) {
            throw new IllegalArgumentException("CIA id namespace/path columns must either both be null or both be set");
        }
        return read(namespace, path);
    }

}
