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
 * Runtime libraries that are not shaded into the plugin jar are resolved here before core classes need them.
 */
@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class CiaPaperLoader implements PluginLoader {

    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        final var resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder(
                "central",
                "default",
                MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
        ).build());

        // Kotlin Standard Library
        resolver.addDependency(new Dependency(
                new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.3.10"), null
        ));

        // Reflect and Coroutine
//        resolver.addDependency(new Dependency(
//                new DefaultArtifact("org.jetbrains.kotlin:kotlin-reflect:2.3.10"), null
//        ));
//        resolver.addDependency(new Dependency(
//                new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2"), null
//        ));

        // Database connection pool and JDBC drivers used by the database runtime.
        resolver.addDependency(new Dependency(
                new DefaultArtifact("com.zaxxer:HikariCP:6.3.2"), null
        ));
        resolver.addDependency(new Dependency(
                new DefaultArtifact("org.xerial:sqlite-jdbc:3.50.3.0"), null
        ));
        resolver.addDependency(new Dependency(
                new DefaultArtifact("com.h2database:h2:2.3.232"), null
        ));
        resolver.addDependency(new Dependency(
                new DefaultArtifact("com.mysql:mysql-connector-j:9.4.0"), null
        ));
        resolver.addDependency(new Dependency(
                new DefaultArtifact("org.postgresql:postgresql:42.7.8"), null
        ));

        classpathBuilder.addLibrary(resolver);
    }

}
