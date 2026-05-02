package top.ourisland.creepersiarena.example;

import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.ICiaExtension;
import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;

/**
 * Minimal CIA extension used as a packaging and loader example.
 * <p>
 * This extension intentionally registers no gameplay content. It proves that a non-Paper CIA extension jar can be
 * discovered through generated {@code cia-extension.yml}, found through {@link java.util.ServiceLoader}, and called
 * through the extension lifecycle.
 */
@CiaExtensionInfo(
        id = "cia-example-extension",
        name = "CreepersIArena Example Extension",
        apiVersion = 1,
        authors = {"Our Island"}
)
public final class ExampleCiaExtension implements ICiaExtension {

    @Override
    public void onLoad(ICiaExtensionContext context) {
        context.registerAnnotated("top.ourisland.creepersiarena.example");
    }

}
