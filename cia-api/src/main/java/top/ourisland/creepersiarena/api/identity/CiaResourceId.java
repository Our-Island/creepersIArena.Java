package top.ourisland.creepersiarena.api.identity;

/**
 * Marker for a domain-specific globally namespaced resource identifier.
 */
public interface CiaResourceId {

    default CiaNamespace namespace() {
        return key().namespace();
    }

    CiaKey key();

    default ResourcePath path() {
        return key().path();
    }

    default String asString() {
        return key().asString();
    }

}
