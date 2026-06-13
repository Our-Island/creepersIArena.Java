package top.ourisland.creepersiarena.api.metadata;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.job.JobId;

public record JobMetadata(
        JobId id,
        boolean enabledByDefault
) {

    public static JobMetadata of(@NonNull Class<?> type) {
        var ann = type.getAnnotation(CiaJobDef.class);
        if (ann == null) {
            throw new IllegalStateException("Missing @CiaJobDef on " + type.getName());
        }
        return new JobMetadata(JobId.parse(ann.id()), ann.enabledByDefault());
    }

}
