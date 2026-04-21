package top.ourisland.creepersiarena.core.component.metadata;

import top.ourisland.creepersiarena.core.component.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.SkillType;

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
