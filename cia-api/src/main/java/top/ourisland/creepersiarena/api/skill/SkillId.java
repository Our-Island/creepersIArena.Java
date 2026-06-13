package top.ourisland.creepersiarena.api.skill;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record SkillId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull SkillId parse(String raw) {
        return new SkillId(CiaKey.parse(raw));
    }

    public static @NonNull SkillId of(CiaKey key) {
        return new SkillId(key);
    }

    public static @NonNull SkillId of(
            CiaNamespace namespace,
            String path
    ) {
        return new SkillId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
