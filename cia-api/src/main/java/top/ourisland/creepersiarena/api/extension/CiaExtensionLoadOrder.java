package top.ourisland.creepersiarena.api.extension;

/**
 * Relative load position requested by a CIA extension descriptor.
 * <p>
 * This value is metadata only at descriptor-reading time. Dependency resolution and actual loading are handled by the
 * runtime extension manager in later startup stages.
 */
public enum CiaExtensionLoadOrder {

    /**
     * Load before normal extensions when dependency ordering allows it.
     */
    EARLY,

    /**
     * Default load position.
     */
    NORMAL,

    /**
     * Load after normal extensions when dependency ordering allows it.
     */
    LATE;

    /**
     * Parses a descriptor value into a load order.
     *
     * @param raw descriptor value; blank values default to {@link #NORMAL}
     *
     * @return parsed load order
     *
     * @throws IllegalArgumentException when the value is not a known load order
     */
    public static CiaExtensionLoadOrder parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return NORMAL;
        }

        return CiaExtensionLoadOrder.valueOf(raw.trim().toUpperCase());
    }

}
