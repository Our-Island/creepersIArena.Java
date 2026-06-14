package top.ourisland.creepersiarena.core.bootstrap.discovery;

import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.identity.RegistrationOwnerAuthority;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.metadata.JobMetadata;
import top.ourisland.creepersiarena.api.metadata.ModeMetadata;
import top.ourisland.creepersiarena.api.metadata.SkillMetadata;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

public final class AnnotationComponentScanner {

    public void scanInto(
            @lombok.NonNull Plugin plugin,
            @lombok.NonNull String basePackage,
            @lombok.NonNull ComponentCatalog catalog
    ) {
        scanInto(plugin, basePackage, catalog, RegistrationOwnerAuthority.core());
    }

    public void scanInto(
            @lombok.NonNull Plugin plugin,
            @lombok.NonNull String basePackage,
            @lombok.NonNull ComponentCatalog catalog,
            @lombok.NonNull RegistrationOwner owner
    ) {
        try {
            var root = Path.of(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            scanInto(plugin.getClass().getClassLoader(), root, basePackage, catalog, true, owner);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Unable to resolve plugin code source", ex);
        }
    }

    private void scanInto(
            ClassLoader classLoader,
            Path codeSource,
            String basePackage,
            ComponentCatalog catalog,
            boolean includeBootstrapModules,
            RegistrationOwner owner
    ) {
        var classNames = discoverClassNames(codeSource, basePackage);
        classNames.sort(Comparator.naturalOrder());

        List<Class<?>> types = new ArrayList<>(classNames.size());
        for (var className : classNames) {
            try {
                types.add(Class.forName(className, false, classLoader));
            } catch (LinkageError error) {
                throw new IllegalStateException("Unable to link annotated CIA component " + className, error);
            } catch (ClassNotFoundException exception) {
                throw new IllegalStateException("Unable to load annotated CIA component " + className, exception);
            }
        }

        try {
            if (includeBootstrapModules) {
                for (var type : types) registerBootstrapModule(type, catalog, owner);
            }
            for (var type : types) registerJob(type, catalog, owner);
            for (var type : types) registerSkill(type, catalog, owner);
            for (var type : types) registerMode(type, catalog, owner);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to instantiate annotated CIA component", ex);
        }
    }

    private void registerBootstrapModule(
            Class<?> type,
            ComponentCatalog catalog,
            RegistrationOwner owner
    ) throws ReflectiveOperationException {
        if (!type.isAnnotationPresent(CiaBootstrapModule.class)) return;
        requireConcreteImplementation(type, CiaBootstrapModule.class, IBootstrapModule.class);
        if (owner != RegistrationOwnerAuthority.core()) {
            throw new IllegalArgumentException("Extensions cannot register bootstrap modules: " + type.getName());
        }
        catalog.registerModule((IBootstrapModule) instantiate(type));
    }

    private void registerJob(
            Class<?> type,
            ComponentCatalog catalog,
            RegistrationOwner owner
    ) throws ReflectiveOperationException {
        if (!type.isAnnotationPresent(CiaJobDef.class)) return;
        requireConcreteImplementation(type, CiaJobDef.class, IJob.class);
        var job = (IJob) instantiate(type);
        var annotatedId = JobMetadata.of(type).id();
        if (!annotatedId.equals(job.id())) {
            throw new IllegalArgumentException(
                    "Annotated job id %s does not match runtime id %s on %s".formatted(
                            annotatedId,
                            job.id(),
                            type.getName()
                    )
            );
        }
        catalog.registerJob(owner, job);
    }

    private void registerSkill(
            Class<?> type,
            ComponentCatalog catalog,
            RegistrationOwner owner
    ) throws ReflectiveOperationException {
        if (!type.isAnnotationPresent(CiaSkillDef.class)) return;
        requireConcreteImplementation(type, CiaSkillDef.class, ISkillDefinition.class);
        var skill = (ISkillDefinition) instantiate(type);
        var metadata = SkillMetadata.of(type);
        if (!metadata.id().equals(skill.id()) || !metadata.job().equals(skill.jobId())) {
            throw new IllegalArgumentException(
                    "Annotated skill metadata %s -> %s does not match runtime metadata %s -> %s on %s".formatted(
                            metadata.id(),
                            metadata.job(),
                            skill.id(),
                            skill.jobId(),
                            type.getName()
                    )
            );
        }
        catalog.registerSkill(owner, skill);
    }

    private void registerMode(
            Class<?> type,
            ComponentCatalog catalog,
            RegistrationOwner owner
    ) throws ReflectiveOperationException {
        if (!type.isAnnotationPresent(CiaModeDef.class)) return;
        requireConcreteImplementation(type, CiaModeDef.class, IGameMode.class);
        var mode = (IGameMode) instantiate(type);
        var annotatedId = ModeMetadata.of(type).id();
        if (!annotatedId.equals(mode.mode())) {
            throw new IllegalArgumentException(
                    "Annotated mode id %s does not match runtime id %s on %s".formatted(
                            annotatedId,
                            mode.mode(),
                            type.getName()
                    )
            );
        }
        catalog.registerMode(owner, mode);
    }

    private void requireConcreteImplementation(
            Class<?> type,
            Class<? extends Annotation> annotation,
            Class<?> requiredType
    ) {
        if (!requiredType.isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    "@%s type must implement %s: %s".formatted(
                            annotation.getSimpleName(),
                            requiredType.getName(),
                            type.getName()
                    )
            );
        }
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers()) || type.isAnonymousClass()) {
            throw new IllegalArgumentException(
                    "@%s type must be a concrete class: %s".formatted(
                            annotation.getSimpleName(),
                            type.getName()
                    )
            );
        }
    }

    private List<String> discoverClassNames(Path root, String basePackage) {
        String pkgPath = basePackage.replace('.', '/');
        List<String> out = new ArrayList<>();

        try {
            if (Files.isDirectory(root)) {
                var start = root.resolve(pkgPath);
                if (!Files.exists(start)) return out;
                try (var stream = Files.walk(start)) {
                    stream.filter(p -> p.toString().endsWith(".class"))
                            .filter(p -> !p.getFileName().toString().contains("$"))
                            .forEach(p -> out.add(toClassName(root, p)));
                }
            } else {
                try (var jar = new JarFile(root.toFile())) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        String name = entry.getName();
                        if (!name.startsWith(pkgPath) || !name.endsWith(".class") || name.contains("$")) continue;
                        out.add(name.substring(0, name.length() - 6).replace('/', '.'));
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to scan CIA components under " + root, ex);
        }

        return out;
    }

    private String toClassName(Path root, Path path) {
        String relative = root.relativize(path).toString();
        return relative.substring(0, relative.length() - 6).replace('/', '.').replace('\\', '.');
    }

    private Object instantiate(Class<?> type) throws ReflectiveOperationException {
        var constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    public void scanInto(
            @lombok.NonNull ClassLoader classLoader,
            @lombok.NonNull Path codeSource,
            @lombok.NonNull String basePackage,
            @lombok.NonNull ComponentCatalog catalog,
            @lombok.NonNull RegistrationOwner owner
    ) {
        scanInto(classLoader, codeSource, basePackage, catalog, false, owner);
    }

}
