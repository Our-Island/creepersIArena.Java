package top.ourisland.creepersiarena.example;

import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.ICiaExtension;

/**
 * Minimal CIA extension used as a packaging and loader example.
 * <p>
 * This extension intentionally registers no gameplay content. It proves that a non-Paper CIA extension jar can be
 * discovered through {@code cia-extension.yml}, found through {@link java.util.ServiceLoader}, and called through the
 * extension lifecycle.
 */
public final class ExampleCiaExtension implements ICiaExtension {

    @Override
    public void onLoad(ICiaExtensionContext context) {
        context.registerAnnotated("top.ourisland.creepersiarena.example");
    }

}
