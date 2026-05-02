package top.ourisland.creepersiarena.api.extension;

import java.util.List;
import java.util.Objects;

/**
 * Parsed metadata from a CIA extension jar's {@code cia-extension.yml} file.
 * <p>
 * The descriptor is intentionally part of {@code cia-api}: build tools, diagnostics, future extension loaders and addon
 * authors can all refer to the same stable metadata model without depending on {@code cia-core} internals.
 *
 * @param id           stable machine-readable extension id
 * @param name         human-readable extension name
 * @param version      extension version string
 * @param mainClass    fully-qualified {@link CiaExtension} implementation class name
 * @param apiVersion   integer CIA extension descriptor/API version
 * @param ciaVersion   supported CreepersIArena version or version range
 * @param authors      display names of authors
 * @param dependencies required and optional extension dependencies
 * @param loadOrder    requested relative load order
 */
public record CiaExtensionDescriptor(
        String id,
        String name,
        String version,
        String mainClass,
        int apiVersion,
        String ciaVersion,
        List<String> authors,
        List<CiaExtensionDependency> dependencies,
        CiaExtensionLoadOrder loadOrder
) {

    /**
     * Extension descriptor resource name inside a CIA extension jar.
     */
    public static final String DESCRIPTOR_ENTRY = "cia-extension.yml";

    /**
     * First supported descriptor format version.
     */
    public static final int CURRENT_API_VERSION = 1;

    public CiaExtensionDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(mainClass, "mainClass");
        Objects.requireNonNull(ciaVersion, "ciaVersion");
        authors = List.copyOf(Objects.requireNonNull(authors, "authors"));
        dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        Objects.requireNonNull(loadOrder, "loadOrder");
    }

    /**
     * Returns only required dependency ids.
     *
     * @return required dependencies in descriptor order
     */
    public List<String> requiredDependencyIds() {
        return dependencies.stream()
                .filter(dependency -> !dependency.optional())
                .map(CiaExtensionDependency::id)
                .toList();
    }

    /**
     * Returns only optional dependency ids.
     *
     * @return optional dependencies in descriptor order
     */
    public List<String> optionalDependencyIds() {
        return dependencies.stream()
                .filter(CiaExtensionDependency::optional)
                .map(CiaExtensionDependency::id)
                .toList();
    }

}
