package top.ourisland.creepersiarena.core.component.metadata;

import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;

import java.util.Arrays;
import java.util.List;

public record BootstrapModuleMetadata(
        String name,
        int order,
        List<Class<? extends IBootstrapModule>> after
) {

    public static BootstrapModuleMetadata of(Class<?> type) {
        var ann = type.getAnnotation(CiaBootstrapModule.class);
        if (ann == null) {
            throw new IllegalStateException("Missing @CiaBootstrapModule on " + type.getName());
        }
        return new BootstrapModuleMetadata(
                ann.name().trim().toLowerCase(),
                ann.order(),
                List.copyOf(Arrays.asList(ann.after()))
        );
    }

}
