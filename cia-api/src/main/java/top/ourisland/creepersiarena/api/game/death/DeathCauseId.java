package top.ourisland.creepersiarena.api.game.death;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;
import top.ourisland.creepersiarena.api.skill.SkillId;

/**
 * Stable, globally namespaced death cause identifier.
 */
public record DeathCauseId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull DeathCauseId parse(String raw) {
        return new DeathCauseId(CiaKey.parse(raw));
    }

    public static @NonNull DeathCauseId of(CiaKey key) {
        return new DeathCauseId(key);
    }

    public static @NonNull DeathCauseId accident(String path) {
        return of(CiaNamespace.CORE, "accident/" + path);
    }

    public static @NonNull DeathCauseId of(
            CiaNamespace namespace,
            String path
    ) {
        return new DeathCauseId(CiaKey.of(namespace, path));
    }

    public static @NonNull DeathCauseId combat(String path) {
        return of(CiaNamespace.CORE, "combat/" + path);
    }

    public static @NonNull DeathCauseId skill(@lombok.NonNull SkillId skillId) {
        return of(skillId.namespace(), "skill/" + skillId.path().value());
    }

    public static @NonNull DeathCauseId custom(
            CiaNamespace namespace,
            String path
    ) {
        return of(namespace, path);
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
