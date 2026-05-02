package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.CiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtension;

/**
 * Entry point for CreepersIArena's bundled gameplay content.
 * <p>
 * The default content is intentionally loaded through the same annotation path as external CIA extension jars. This
 * keeps built-in jobs, skills and modes on the same registration surface that third-party content uses.
 */
public final class DefaultContentAddon implements CiaExtension {

    private static final String ROOT_PACKAGE = "top.ourisland.creepersiarena";

    @Override
    public void onLoad(CiaExtensionContext context) {
        context.registerAnnotated(ROOT_PACKAGE);
    }

}
