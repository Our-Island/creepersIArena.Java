package top.ourisland.creepersiarena.core.extension.metadata;

/**
 * Thrown when a CIA extension descriptor cannot be read or validated.
 */
public final class CiaExtensionDescriptorException extends RuntimeException {

    /**
     * Creates a descriptor exception.
     *
     * @param message error message
     */
    public CiaExtensionDescriptorException(String message) {
        super(message);
    }

    /**
     * Creates a descriptor exception with a cause.
     *
     * @param message error message
     * @param cause   underlying cause
     */
    public CiaExtensionDescriptorException(String message, Throwable cause) {
        super(message, cause);
    }

}
