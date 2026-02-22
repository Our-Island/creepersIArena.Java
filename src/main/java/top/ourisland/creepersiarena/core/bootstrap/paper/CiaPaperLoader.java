package top.ourisland.creepersiarena.core.bootstrap.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
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
@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class CiaPaperLoader implements PluginLoader {

    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder(
                "central", "default",
                MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
        ).build());

        // Kotlin Standard Library
        resolver.addDependency(new Dependency(
                new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.3.10"), null)
        );

        // Reflect and Coroutine
//         resolver.addDependency(new Dependency(
//                 new DefaultArtifact("org.jetbrains.kotlin:kotlin-reflect:2.3.10"), null)
//         );
//         resolver.addDependency(new Dependency(
//                 new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2"), null)
//         );

        classpathBuilder.addLibrary(resolver);
    }

}
