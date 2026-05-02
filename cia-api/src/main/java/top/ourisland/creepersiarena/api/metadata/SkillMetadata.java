package top.ourisland.creepersiarena.api.metadata;

import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.SkillType;

public record SkillMetadata(
        String id,
        JobId job,
        SkillType type,
        int slot,
        int defaultCooldown
) {

    public static SkillMetadata of(Class<?> type) {
        var ann = type.getAnnotation(CiaSkillDef.class);
        if (ann == null) {
            throw new IllegalStateException("Missing @CiaSkillDef on " + type.getName());
        }
        return new SkillMetadata(
                ann.id().trim().toLowerCase(),
                JobId.of(ann.job()),
                ann.type(),
                ann.slot(),
                ann.defaultCooldown()
        );
    }

}
