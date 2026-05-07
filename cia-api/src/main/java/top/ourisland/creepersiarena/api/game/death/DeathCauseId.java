package top.ourisland.creepersiarena.api.game.death;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record DeathCauseId(
        NamespacedKey key
) {

    public static final String DEFAULT_NAMESPACE = "cia";

    public DeathCauseId {
        Objects.requireNonNull(key, "key");
    }

    public static DeathCauseId of(String path) {
        return of(DEFAULT_NAMESPACE, path);
    }

    public static DeathCauseId accident(String path) {
        return of("accident/" + normalizePath(path));
    }

    public static DeathCauseId combat(String path) {
        return of("combat/" + normalizePath(path));
    }

    public static DeathCauseId skill(String jobId, String skillId) {
        return skill(DEFAULT_NAMESPACE, jobId, skillId);
    }

    public static DeathCauseId skill(String namespace, String jobId, String skillId) {
        return of(
                normalizeNamespace(namespace),
                "skill/" + stripNamespace(jobId) + "/" + normalizePath(skillId)
        );
    }

    public static DeathCauseId custom(String namespace, String path) {
        return of(namespace, path);
    }

    public static DeathCauseId of(String namespace, String path) {
        return new DeathCauseId(new NamespacedKey(
                normalizeNamespace(namespace),
                normalizePath(path)
        ));
    }

    private static String normalizeNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) return DEFAULT_NAMESPACE;
        return namespace.trim();
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Death cause path must not be blank");
        }
        return stripNamespace(path.trim());
    }

    private static String stripNamespace(String value) {
        int separator = value.indexOf(':');
        if (separator < 0) return value;
        if (separator >= value.length() - 1) {
            throw new IllegalArgumentException("Namespaced value must include a path: " + value);
        }
        return value.substring(separator + 1);
    }

    public static DeathCauseId parse(String raw) {
        if (raw == null || raw.isBlank()) return StandardDeathCauses.GENERIC;

        String trimmed = raw.trim();
        int separator = trimmed.indexOf(':');
        if (separator < 0) return of(trimmed);
        if (separator == 0 || separator >= trimmed.length() - 1) return StandardDeathCauses.GENERIC;

        try {
            return of(trimmed.substring(0, separator), trimmed.substring(separator + 1));
        } catch (IllegalArgumentException ignored) {
            return StandardDeathCauses.GENERIC;
        }
    }

    public String namespace() {
        return key.getNamespace();
    }

    public String value() {
        return key.getKey();
    }

    @Override
    public @NonNull String toString() {
        return key.asString();
    }

}
