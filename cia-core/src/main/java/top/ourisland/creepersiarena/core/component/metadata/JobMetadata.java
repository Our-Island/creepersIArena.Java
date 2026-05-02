package top.ourisland.creepersiarena.core.component.metadata;

import top.ourisland.creepersiarena.core.component.annotation.CiaJobDef;
import top.ourisland.creepersiarena.job.JobId;

public record JobMetadata(
        JobId id,
        boolean enabledByDefault
) {

    public static JobMetadata of(Class<?> type) {
        var ann = type.getAnnotation(CiaJobDef.class);
        if (ann == null) {
            throw new IllegalStateException("Missing @CiaJobDef on " + type.getName());
        }
        return new JobMetadata(JobId.of(ann.id()), ann.enabledByDefault());
    }

}
