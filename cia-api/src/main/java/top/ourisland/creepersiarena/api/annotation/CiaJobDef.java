package top.ourisland.creepersiarena.api.annotation;

import java.lang.annotation.*;

/**
 * Marks a class as a discoverable job definition and declares its immutable registration metadata.
 * <p>
 * The component discovery layer scans built-in classes (and potentially extension-provided classes through the
 * extension API) for {@code @CiaJobDef}. The annotation exists so jobs can be registered from code-first metadata
 * instead of a hard-coded enum list.
 *
 * <h2>What belongs here</h2>
 * This annotation contains only <em>static</em> information needed to register the job in the catalog:
 * <ul>
 *     <li>its stable namespaced id</li>
 *     <li>whether it should be considered enabled before config-based filtering is applied</li>
 * </ul>
 * Behaviour, equipment, skills and lobby presentation remain the responsibility of the annotated job class itself.
 *
 * <h2>ID stability</h2>
 * The id returned by {@link #id()} is the public registry identity of the job. It is used by config, player session
 * state, language-key generation and skill ownership metadata, so changing it is effectively a breaking change for any
 * existing data or extension integration.
 *
 * @see top.ourisland.creepersiarena.api.CiaExtensionContext#registerAnnotated(String)
 * @see top.ourisland.creepersiarena.api.metadata.JobMetadata
 * @see top.ourisland.creepersiarena.api.job.IJob
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CiaJobDef {

    /**
     * Returns the stable namespaced registry id of the job.
     * <p>
     * Built-in content uses the {@code cia} namespace, for example {@code cia:creeper}. Extensions should use their own
     * namespace to avoid collisions. The id is later normalized by the i18n helpers when language keys are built, but
     * the original value remains the runtime identity.
     *
     * @return globally unique job id
     */
    String id();

    /**
     * Returns whether the job should be considered enabled before configuration overrides are applied.
     * <p>
     * This value acts as a catalog default only. The final enabled/disabled state may still be overridden by plugin
     * configuration, feature flags or extension-specific registration rules.
     *
     * @return {@code true} if the job is opt-in by default at discovery time
     */
    boolean enabledByDefault() default true;

}
