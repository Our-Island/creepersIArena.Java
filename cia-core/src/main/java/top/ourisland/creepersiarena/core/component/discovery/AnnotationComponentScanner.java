package top.ourisland.creepersiarena.core.component.discovery;

import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class AnnotationComponentScanner {

    public void scanInto(
            @lombok.NonNull Plugin plugin,
            @lombok.NonNull String basePackage,
            @lombok.NonNull ComponentCatalog catalog
    ) {
        scanInto(plugin, basePackage, catalog, RegisteredComponent.CORE_OWNER);
    }

    public void scanInto(
            @lombok.NonNull Plugin plugin,
            @lombok.NonNull String basePackage,
            @lombok.NonNull ComponentCatalog catalog,
            @lombok.NonNull String ownerId
    ) {
        try {
            Path root = Path.of(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            scanInto(plugin.getClass().getClassLoader(), root, basePackage, catalog, true, ownerId);
        } catch (URISyntaxException _) {
        }
    }

    private void scanInto(
            ClassLoader classLoader,
            Path codeSource,
            String basePackage,
            ComponentCatalog catalog,
            boolean includeBootstrapModules,
            String ownerId
    ) {
        var classNames = discoverClassNames(codeSource, basePackage);
        classNames.sort(Comparator.naturalOrder());

        for (String className : classNames) {
            try {
                Class<?> type = Class.forName(className, false, classLoader);
                tryRegister(type, catalog, includeBootstrapModules, ownerId);
            } catch (Throwable _) {
            }
        }
    }

    private List<String> discoverClassNames(Path root, String basePackage) {
        String pkgPath = basePackage.replace('.', '/');
        List<String> out = new ArrayList<>();

        try {
            if (Files.isDirectory(root)) {
                Path start = root.resolve(pkgPath);
                if (!Files.exists(start)) return out;
                try (var stream = Files.walk(start)) {
                    stream.filter(p -> p.toString().endsWith(".class"))
                            .filter(p -> !p.getFileName().toString().contains("$"))
                            .forEach(p -> out.add(toClassName(root, p)));
                }
            } else {
                try (JarFile jar = new JarFile(root.toFile())) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry e = entries.nextElement();
                        String name = e.getName();
                        if (!name.startsWith(pkgPath) || !name.endsWith(".class") || name.contains("$")) continue;
                        out.add(name.substring(0, name.length() - 6).replace('/', '.'));
                    }
                }
            }
        } catch (IOException _) {
        }

        return out;
    }

    private void tryRegister(
            Class<?> type,
            ComponentCatalog catalog,
            boolean includeBootstrapModules,
            String ownerId
    ) throws ReflectiveOperationException {
        if (type.isInterface() || java.lang.reflect.Modifier.isAbstract(type.getModifiers()) || type.isAnonymousClass()) {
            return;
        }

        if (includeBootstrapModules
                && type.isAnnotationPresent(CiaBootstrapModule.class)
                && IBootstrapModule.class.isAssignableFrom(type)) {
            catalog.registerModule(ownerId, (IBootstrapModule) instantiate(type));
        }
        if (type.isAnnotationPresent(CiaJobDef.class) && IJob.class.isAssignableFrom(type)) {
            catalog.registerJob(ownerId, (IJob) instantiate(type));
        }
        if (type.isAnnotationPresent(CiaSkillDef.class) && ISkillDefinition.class.isAssignableFrom(type)) {
            catalog.registerSkill(ownerId, (ISkillDefinition) instantiate(type));
        }
        if (type.isAnnotationPresent(CiaModeDef.class) && IGameMode.class.isAssignableFrom(type)) {
            catalog.registerMode(ownerId, (IGameMode) instantiate(type));
        }
    }

    private String toClassName(Path root, Path p) {
        String rel = root.relativize(p).toString();
        return rel.substring(0, rel.length() - 6).replace('/', '.').replace('\\', '.');
    }

    private Object instantiate(Class<?> type) throws ReflectiveOperationException {
        var ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }

    public void scanInto(
            @lombok.NonNull ClassLoader classLoader,
            @lombok.NonNull Path codeSource,
            @lombok.NonNull String basePackage,
            @lombok.NonNull ComponentCatalog catalog
    ) {
        scanInto(classLoader, codeSource, basePackage, catalog, false, RegisteredComponent.CORE_OWNER);
    }

    public void scanInto(
            @lombok.NonNull ClassLoader classLoader,
            @lombok.NonNull Path codeSource,
            @lombok.NonNull String basePackage,
            @lombok.NonNull ComponentCatalog catalog,
            @lombok.NonNull String ownerId
    ) {
        scanInto(classLoader, codeSource, basePackage, catalog, false, ownerId);
    }

}
