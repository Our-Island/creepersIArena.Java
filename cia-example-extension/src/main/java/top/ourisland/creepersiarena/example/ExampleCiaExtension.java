package top.ourisland.creepersiarena.example;

import top.ourisland.creepersiarena.api.CiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtension;

/**
 * Minimal CIA extension used as a packaging and loader example.
 * <p>
 * This extension intentionally registers no gameplay content. It proves that a non-Paper CIA extension jar can be
 * discovered through {@code cia-extension.yml}, found through {@link java.util.ServiceLoader}, and called through the
 * extension lifecycle.
 */
public final class ExampleCiaExtension implements CiaExtension {

    @Override
    public void onLoad(CiaExtensionContext context) {
        context.registerAnnotated("top.ourisland.creepersiarena.example");
    }

}
