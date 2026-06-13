package top.ourisland.creepersiarena.core.bootstrap.discovery;

import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;

import java.io.IOException;
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
        scanInto(plugin, basePackage, catalog, RegistrationOwner.CORE);
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

        for (var className : classNames) {
            try {
                Class<?> type = Class.forName(className, false, classLoader);
                tryRegister(type, catalog, includeBootstrapModules, owner);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Unable to instantiate annotated CIA component " + className, ex);
            } catch (LinkageError ex) {
                throw new IllegalStateException("Unable to link annotated CIA component " + className, ex);
            }
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

    private void tryRegister(
            Class<?> type,
            ComponentCatalog catalog,
            boolean includeBootstrapModules,
            RegistrationOwner owner
    ) throws ReflectiveOperationException {
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers()) || type.isAnonymousClass()) {
            return;
        }

        if (
                includeBootstrapModules
                        && type.isAnnotationPresent(CiaBootstrapModule.class)
                        && IBootstrapModule.class.isAssignableFrom(type)
        ) {
            if (!RegistrationOwner.CORE.equals(owner)) {
                throw new IllegalArgumentException("Extensions cannot register bootstrap modules: " + type.getName());
            }
            catalog.registerModule((IBootstrapModule) instantiate(type));
        }
        if (type.isAnnotationPresent(CiaJobDef.class) && IJob.class.isAssignableFrom(type)) {
            catalog.registerJob(owner, (IJob) instantiate(type));
        }
        if (type.isAnnotationPresent(CiaSkillDef.class) && ISkillDefinition.class.isAssignableFrom(type)) {
            catalog.registerSkill(owner, (ISkillDefinition) instantiate(type));
        }
        if (type.isAnnotationPresent(CiaModeDef.class) && IGameMode.class.isAssignableFrom(type)) {
            catalog.registerMode(owner, (IGameMode) instantiate(type));
        }
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
