package top.ourisland.creepersiarena.api.extension.annotation;

import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compile-time metadata for a CIA extension entry point.
 * <p>
 * Annotate exactly one {@code ICiaExtension} implementation in an extension module. The CIA annotation processor
 * generates both {@code cia-extension.yml} and the {@code ServiceLoader} provider file used by the runtime loader.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CiaExtensionInfo {

    /**
     * Stable machine-readable extension id.
     *
     * @return extension id using {@code [a-z0-9_.-]}
     */
    String id();

    /**
     * Human-readable extension name.
     *
     * @return display name
     */
    String name();

    /**
     * Extension version. When empty, the processor uses {@code -Acia.extension.version=<version>}.
     *
     * @return extension version or empty to use the compiler option
     */
    String version() default "";

    /**
     * CIA extension descriptor/API version.
     *
     * @return descriptor API version
     */
    int apiVersion() default 1;

    /**
     * Supported CreepersIArena version or version range. When empty, the generated descriptor uses the extension
     * version.
     *
     * @return supported CreepersIArena version expression
     */
    String ciaVersion() default "";

    /**
     * Display names of extension authors.
     *
     * @return authors
     */
    String[] authors() default {};

    /**
     * Required extension dependency ids.
     *
     * @return required dependency ids
     */
    String[] requiredDependencies() default {};

    /**
     * Optional extension dependency ids.
     *
     * @return optional dependency ids
     */
    String[] optionalDependencies() default {};

    /**
     * Requested relative load order.
     *
     * @return load order
     */
    CiaExtensionLoadOrder loadOrder() default CiaExtensionLoadOrder.NORMAL;

}
