package top.ourisland.creepersiarena.api.extension;

import java.util.Objects;

/**
 * Dependency declaration from {@code cia-extension.yml}.
 *
 * @param id       extension id required or optionally integrated with by the declaring extension
 * @param optional whether the dependency is optional
 */
public record CiaExtensionDependency(
        String id,
        boolean optional
) {

    public CiaExtensionDependency {
        Objects.requireNonNull(id, "id");
    }

    /**
     * Creates a required dependency declaration.
     *
     * @param id target extension id
     *
     * @return dependency declaration
     */
    public static CiaExtensionDependency required(String id) {
        return new CiaExtensionDependency(id, false);
    }

    /**
     * Creates an optional dependency declaration.
     *
     * @param id target extension id
     *
     * @return dependency declaration
     */
    public static CiaExtensionDependency optional(String id) {
        return new CiaExtensionDependency(id, true);
    }

}
