package top.ourisland.creepersiarena.bootstrap.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import org.jspecify.annotations.NonNull;

/**
 * Paper Plugin loader entrypoint.
 * <p>
 * Currently, dynamic downloading/injection of runtime dependencies is not required;
 * the implementation can remain empty.
 * <p>
 * In the future, if libraries (JarLibrary / MavenLibraryResolver) need to be added at
 * runtime, this will be done here.
 */
@SuppressWarnings("UnstableApiUsage")
public final class CiaPaperLoader implements PluginLoader {

    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        // No runtime libraries to add.
    }
}
