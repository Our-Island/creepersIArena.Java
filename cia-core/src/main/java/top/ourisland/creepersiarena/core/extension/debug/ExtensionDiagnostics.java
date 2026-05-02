package top.ourisland.creepersiarena.core.extension.debug;

import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.component.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.core.component.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.extension.loading.CiaExtensionLoadFailure;
import top.ourisland.creepersiarena.core.extension.loading.CiaExtensionManager;
import top.ourisland.creepersiarena.core.extension.loading.LoadedCiaExtension;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

public final class ExtensionDiagnostics {

    private ExtensionDiagnostics() {
    }

    public static List<String> infoLines(BootstrapRuntime rt, String id) {
        var manager = rt.requireService(CiaExtensionManager.class);
        var normalized = normalize(id);
        var loaded = manager.loadedExtension(normalized);
        if (loaded != null) {
            return loadedInfoLines(rt, loaded);
        }

        var failure = manager.loadFailure(normalized);
        if (failure != null) {
            return failureInfoLines(failure);
        }

        return List.of("CIA extension not found: " + id);
    }

    private static String normalize(String id) {
        return RegisteredComponent.normalizeOwnerId(id);
    }

    private static List<String> loadedInfoLines(BootstrapRuntime rt, LoadedCiaExtension loaded) {
        var descriptor = loaded.descriptor();
        var snapshot = loaded.registrations();
        var lines = new ArrayList<String>();
        lines.add("id=" + descriptor.id());
        lines.add("status=" + (loaded.enabled() ? "ENABLED" : "LOADED"));
        lines.add("name=" + descriptor.name());
        lines.add("version=" + descriptor.version());
        lines.add("main=" + descriptor.mainClass());
        lines.add("api-version=" + descriptor.apiVersion());
        lines.add("cia-version=" + descriptor.ciaVersion());
        lines.add("load-order=" + descriptor.loadOrder());
        lines.add("authors=" + descriptor.authors());
        lines.add("required-dependencies=" + descriptor.requiredDependencyIds());
        lines.add("optional-dependencies=" + descriptor.optionalDependencyIds());
        lines.add("jar=" + loaded.jarPath());
        lines.add("data-folder=" + loaded.context().dataFolder());
        lines.add("loaded-at=" + loaded.loadedAt());
        appendList(lines, "registered-jobs", snapshot.jobs());
        appendList(lines, "registered-skills", snapshot.skills());
        appendList(lines, "registered-modes", snapshot.modes());
        appendList(lines, "registered-listeners", snapshot.listeners());
        appendList(lines, "installed-resources", snapshot.installedResources());
        appendList(lines, "merged-yaml-resources", snapshot.mergedYamlResources());
        appendList(lines, "merged-properties-resources", snapshot.mergedPropertiesResources());
        appendOwnerRuntimeView(rt, descriptor.id(), lines);
        return lines;
    }

    private static List<String> failureInfoLines(CiaExtensionLoadFailure failure) {
        return List.of(
                "id=" + failure.id(),
                "status=FAILED",
                "jar=" + failure.jarPath(),
                "failed-at=" + failure.failedAt(),
                "message=" + failure.message()
        );
    }

    private static void appendList(List<String> lines, String label, List<String> values) {
        lines.add(label + "=" + sorted(values));
    }

    private static void appendOwnerRuntimeView(BootstrapRuntime rt, String ownerId, List<String> lines) {
        var normalized = normalize(ownerId);
        var catalog = rt.getService(ComponentCatalog.class);
        if (catalog != null) {
            appendList(lines, "catalog-modules", catalog.registeredModules().stream()
                    .filter(component -> ownerMatches(component.ownerId(), normalized))
                    .map(RegisteredComponent::key)
                    .sorted()
                    .toList());
            appendList(lines, "catalog-jobs", catalog.registeredJobs().stream()
                    .filter(component -> ownerMatches(component.ownerId(), normalized))
                    .map(RegisteredComponent::key)
                    .sorted()
                    .toList());
            appendList(lines, "catalog-skills", catalog.registeredSkills().stream()
                    .filter(component -> ownerMatches(component.ownerId(), normalized))
                    .map(RegisteredComponent::key)
                    .sorted()
                    .toList());
            appendList(lines, "catalog-modes", catalog.registeredModes().stream()
                    .filter(component -> ownerMatches(component.ownerId(), normalized))
                    .map(RegisteredComponent::key)
                    .sorted()
                    .toList());
        }
    }

    private static List<String> sorted(List<String> values) {
        return values.stream().sorted().toList();
    }

    private static boolean ownerMatches(String ownerId, String expected) {
        return normalize(ownerId).equals(expected);
    }

    public static Path writeDump(BootstrapRuntime rt) {
        var target = rt.plugin().getDataFolder().toPath().resolve("extension-cache/extensions-dump.txt");
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, String.join(System.lineSeparator(), dumpLines(rt)) + System.lineSeparator(), StandardCharsets.UTF_8);
            return target;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write extension dump: " + target, ex);
        }
    }

    public static List<String> dumpLines(BootstrapRuntime rt) {
        var lines = new ArrayList<String>();
        lines.add("# CreepersIArena extension dump");
        lines.add("generated-at=" + DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now()));
        lines.add("plugin=" + rt.plugin().getName() + " version=" + rt.plugin().getPluginMeta().getVersion());
        lines.add("");
        lines.add("## Extensions");
        lines.addAll(listLines(rt));
        lines.add("");

        var manager = rt.requireService(CiaExtensionManager.class);
        for (var loaded : manager.loadedExtensions().stream()
                .sorted(Comparator.comparing(extension -> extension.descriptor().id()))
                .toList()) {
            lines.add("## Extension " + loaded.descriptor().id());
            lines.addAll(loadedInfoLines(rt, loaded));
            lines.add("");
        }

        if (!manager.loadFailures().isEmpty()) {
            lines.add("## Failed extensions");
            for (var failure : manager.loadFailures()) {
                lines.addAll(failureInfoLines(failure));
                lines.add("");
            }
        }

        lines.add("## Runtime registry owners");
        appendRuntimeOwners(rt, lines);
        return lines;
    }

    public static List<String> listLines(BootstrapRuntime rt) {
        var manager = rt.requireService(CiaExtensionManager.class);
        var lines = new ArrayList<String>();
        lines.add("CIA extensions: loaded=" + manager.loadedExtensions().size() + " failed=" + manager.loadFailures()
                .size());

        for (var loaded : manager.loadedExtensions().stream()
                .sorted(Comparator.comparing(extension -> extension.descriptor().id()))
                .toList()) {
            var descriptor = loaded.descriptor();
            var snapshot = loaded.registrations();
            lines.add("- " + descriptor.id()
                    + " [" + (loaded.enabled() ? "ENABLED" : "LOADED") + "]"
                    + " version=" + descriptor.version()
                    + " jobs=" + snapshot.jobs().size()
                    + " skills=" + snapshot.skills().size()
                    + " modes=" + snapshot.modes().size()
                    + " listeners=" + snapshot.listeners().size()
                    + " resources=" + snapshot.totalResources());
        }

        for (var failure : manager.loadFailures()) {
            lines.add("- " + failure.id() + " [FAILED] " + failure.message());
        }

        if (manager.loadedExtensions().isEmpty() && manager.loadFailures().isEmpty()) {
            lines.add("No CIA extensions have been discovered.");
        }
        return lines;
    }

    private static void appendRuntimeOwners(BootstrapRuntime rt, List<String> lines) {
        var catalog = rt.getService(ComponentCatalog.class);
        if (catalog != null) {
            appendGrouped(lines, "catalog.modules", catalog.registeredModules(), RegisteredComponent::key);
            appendGrouped(lines, "catalog.jobs", catalog.registeredJobs(), RegisteredComponent::key);
            appendGrouped(lines, "catalog.skills", catalog.registeredSkills(), RegisteredComponent::key);
            appendGrouped(lines, "catalog.modes", catalog.registeredModes(), RegisteredComponent::key);
        }

        var jobs = rt.getService(JobManager.class);
        if (jobs != null) {
            appendGrouped(lines, "runtime.jobs", jobs.registeredJobs(), RegisteredComponent::key);
        }

        var skills = rt.getService(SkillRegistry.class);
        if (skills != null) {
            appendGrouped(lines, "runtime.skills", skills.registeredSkills(), RegisteredComponent::key);
        }

        var games = rt.getService(GameManager.class);
        if (games != null) {
            appendGrouped(lines, "runtime.modes", games.registeredModes(), RegisteredComponent::key);
        }
    }

    private static <T> void appendGrouped(
            List<String> lines,
            String title,
            Collection<RegisteredComponent<T>> components,
            Function<RegisteredComponent<T>, String> keyFn
    ) {
        lines.add(title + ":");
        var grouped = new LinkedHashMap<String, List<String>>();
        for (var component : components) {
            grouped.computeIfAbsent(component.ownerId(), ignored -> new ArrayList<>()).add(keyFn.apply(component));
        }
        if (grouped.isEmpty()) {
            lines.add("  <empty>");
            return;
        }
        grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> lines.add("  " + entry.getKey() + " -> " + sorted(entry.getValue())));
    }

}
