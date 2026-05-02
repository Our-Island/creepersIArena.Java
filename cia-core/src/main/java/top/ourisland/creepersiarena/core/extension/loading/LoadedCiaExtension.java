package top.ourisland.creepersiarena.core.extension.loading;

import top.ourisland.creepersiarena.api.CiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtension;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;

import java.nio.file.Path;
import java.time.Instant;

public final class LoadedCiaExtension {

    private final CiaExtensionDescriptor descriptor;
    private final Path jarPath;
    private final CiaExtensionClassLoader classLoader;
    private final CiaExtension extension;
    private final CiaExtensionRuntimeContext context;
    private final Instant loadedAt;
    private boolean enabled;

    public LoadedCiaExtension(
            CiaExtensionDescriptor descriptor,
            Path jarPath,
            CiaExtensionClassLoader classLoader,
            CiaExtension extension,
            CiaExtensionRuntimeContext context
    ) {
        this.descriptor = descriptor;
        this.jarPath = jarPath;
        this.classLoader = classLoader;
        this.extension = extension;
        this.context = context;
        this.loadedAt = Instant.now();
    }

    public CiaExtensionDescriptor descriptor() {
        return descriptor;
    }

    public Path jarPath() {
        return jarPath;
    }

    public CiaExtensionClassLoader classLoader() {
        return classLoader;
    }

    public CiaExtension extension() {
        return extension;
    }

    public CiaExtensionContext context() {
        return context;
    }

    public Instant loadedAt() {
        return loadedAt;
    }

    public boolean enabled() {
        return enabled;
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
