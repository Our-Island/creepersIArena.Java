package top.ourisland.creepersiarena.core.extension.loading;

import top.ourisland.creepersiarena.api.CiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtension;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;

import java.nio.file.Path;

public record LoadedCiaExtension(
        CiaExtensionDescriptor descriptor,
        Path jarPath,
        CiaExtensionClassLoader classLoader,
        CiaExtension extension,
        CiaExtensionContext context
) {

}
