package top.ourisland.creepersiarena.job.skill.runtime;

/**
 * Lightweight internal control-flow exception used to reject a skill activation attempt without treating it as an
 * execution error.
 * <p>
 * This type exists for skills whose outer trigger can only say that activation is <em>possible</em>, while the true
 * gameplay preconditions can only be checked later inside the executor. Typical examples include:
 * <ul>
 *     <li>a skill that requires a recently hit target to still exist,</li>
 *     <li>a teleport skill that requires a live projectile or marker anchor,</li>
 *     <li>a passive skill that only fires when several sibling skills are all cooling down,</li>
 *     <li>or any other effect whose last validation depends on live world / entity state.</li>
 * </ul>
 *
 * <h2>Runtime meaning</h2>
 * Throwing this exception means:
 * <ul>
 *     <li>the skill's outer trigger matched,</li>
 *     <li>the executor intentionally rejected activation as a normal gameplay outcome,</li>
 *     <li>the runtime should stop processing this activation attempt,</li>
 *     <li>and cooldown must <strong>not</strong> be applied.</li>
 * </ul>
 *
 * <p>This exception is therefore not a bug marker and should not be treated like an unexpected failure. It is a
 * dedicated control-flow signal understood by {@link SkillRuntime}.
 *
 * <h2>Allocation policy</h2>
 * The class exposes a reusable singleton instance because it carries no payload, stack trace or suppression state. This
 * keeps the signal cheap to throw from frequently evaluated skills.
 */
public final class SkillActivationRejectedException extends RuntimeException {

    public static final SkillActivationRejectedException INSTANCE = new SkillActivationRejectedException();

    private SkillActivationRejectedException() {
        super(null, null, false, false);
    }

    /**
     * Returns the shared rejection instance.
     * <p>
     * Callers may use this helper to make intent explicit at the throw site:
     * <pre>{@code
     * throw SkillActivationRejectedException.reject();
     * }</pre>
     *
     * @return singleton exception used to signal intentional activation rejection
     */
    public static SkillActivationRejectedException reject() {
        return INSTANCE;
    }

}
