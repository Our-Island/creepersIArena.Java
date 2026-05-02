package top.ourisland.creepersiarena.core.component.annotation;

import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;

import java.lang.annotation.*;

/**
 * Marks a class as a discoverable bootstrap module and declares its install-order metadata.
 * <p>
 * Bootstrap modules are the units used to assemble the plugin's runtime services, listeners and startup stages. This
 * annotation lets the core discover them from source metadata rather than from a manually maintained hard-coded list.
 *
 * <h2>Ordering model</h2>
 * Module discovery uses two complementary signals:
 * <ul>
 *     <li>{@link #order()} for coarse numeric ordering</li>
 *     <li>{@link #after()} for explicit dependency edges between module types</li>
 * </ul>
 * The final resolver can combine both pieces of information to determine a safe startup sequence.
 *
 * <h2>Name usage</h2>
 * {@link #name()} is a human-facing/module-facing identifier used in logs, diagnostics and bootstrap reporting. It does
 * not replace the Java type identity used by {@link #after()}.
 *
 * @see IBootstrapModule
 * @see top.ourisland.creepersiarena.core.component.discovery.ModuleOrderResolver
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CiaBootstrapModule {

    /**
     * Returns the logical/bootstrap name of the module.
     *
     * @return short diagnostic name used for logs and bootstrap reporting
     */
    String name();

    /**
     * Returns the coarse numeric sort order of the module.
     * <p>
     * Lower values are typically installed earlier. This value is a convenience ordering hint, not a full dependency
     * model on its own.
     *
     * @return module order hint
     */
    int order() default 0;

    /**
     * Declares module types that must be ordered before the annotated module.
     * <p>
     * These edges are used by the bootstrap ordering resolver to preserve required dependencies when numeric ordering
     * alone would be ambiguous.
     *
     * @return explicit predecessor module types
     */
    Class<? extends IBootstrapModule>[] after() default {};

}
