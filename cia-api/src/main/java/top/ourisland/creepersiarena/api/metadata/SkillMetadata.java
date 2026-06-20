package top.ourisland.creepersiarena.api.metadata;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.SkillId;
import top.ourisland.creepersiarena.api.skill.SkillType;

public record SkillMetadata(
        SkillId id,
        JobId job,
        SkillType type,
        int slot,
        int defaultCooldown
) {

    public static @NonNull SkillMetadata of(@NonNull Class<?> type) {
        var annotation = type.getAnnotation(CiaSkillDef.class);
        if (annotation == null) {
            throw new IllegalStateException("Missing @CiaSkillDef on " + type.getName());
        }
        return new SkillMetadata(
                SkillId.parse(annotation.id()),
                JobId.parse(annotation.job()),
                annotation.type(),
                annotation.slot(),
                annotation.defaultCooldown()
        );
    }

}
