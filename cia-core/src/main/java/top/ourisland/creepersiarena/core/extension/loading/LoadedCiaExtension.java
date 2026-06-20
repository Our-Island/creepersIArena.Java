package top.ourisland.creepersiarena.core.extension.loading;

import lombok.Getter;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.api.extension.ICiaExtension;

import java.nio.file.Path;
import java.time.Instant;

@Getter
public final class LoadedCiaExtension {

    private final CiaExtensionDescriptor descriptor;
    private final Path jarPath;
    private final CiaExtensionClassLoader classLoader;
    private final ICiaExtension extension;
    private final CiaExtensionRuntimeContext context;
    private final Instant loadedAt;
    private boolean enabled;

    public LoadedCiaExtension(
            CiaExtensionDescriptor descriptor,
            Path jarPath,
            CiaExtensionClassLoader classLoader,
            ICiaExtension extension,
            CiaExtensionRuntimeContext context
    ) {
        this.descriptor = descriptor;
        this.jarPath = jarPath;
        this.classLoader = classLoader;
        this.extension = extension;
        this.context = context;
        this.loadedAt = Instant.now();
    }

    public ICiaExtensionContext context() {
        return context;
    }

    CiaExtensionRuntimeContext runtimeContext() {
        return context;
    }

    void markEnabled() {
        enabled = true;
    }

    void markDisabled() {
        enabled = false;
    }

    public ExtensionRegistrationSnapshot registrations() {
        return context.snapshot();
    }

}
