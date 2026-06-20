package top.ourisland.creepersiarena.core.job.skill.runtime;

import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;

import java.util.Objects;
import java.util.function.Function;

/**
 * Canonical runtime validation for the namespace and job ownership relationship of a skill registration.
 */
public final class SkillRegistrationValidator {

    private SkillRegistrationValidator() {
    }

    public static void validate(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull ISkillDefinition skill,
            @lombok.NonNull Function<JobId, RegistrationOwner> jobOwnerLookup
    ) {
        var skillId = Objects.requireNonNull(skill.id(), "skill.id()");
        var jobId = Objects.requireNonNull(skill.jobId(), "skill.jobId()");

        if (!skillId.namespace().equals(owner.namespace())) {
            throw new IllegalArgumentException(
                    "Skill %s must use owner namespace %s".formatted(
                            skillId,
                            owner.namespace().value()
                    )
            );
        }
        if (!jobId.namespace().equals(owner.namespace())) {
            throw new IllegalArgumentException(
                    "Skill %s cannot target job %s outside owner namespace %s".formatted(
                            skillId,
                            jobId,
                            owner.namespace().value()
                    )
            );
        }

        var expectedPrefix = jobId.path().value() + "/";
        var skillPath = skillId.path().value();
        if (!skillPath.startsWith(expectedPrefix) || skillPath.length() == expectedPrefix.length()) {
            throw new IllegalArgumentException(
                    "Skill %s must be a child path of job %s (expected %s<skill>)".formatted(
                            skillId,
                            jobId,
                            expectedPrefix
                    )
            );
        }

        var registeredJobOwner = jobOwnerLookup.apply(jobId);
        if (registeredJobOwner == null) {
            throw new IllegalArgumentException(
                    "Skill %s references unregistered job %s".formatted(skillId, jobId)
            );
        }
        if (registeredJobOwner != owner) {
            throw new IllegalArgumentException(
                    "Skill %s and job %s must be registered by the same owner; skill owner=%s, job owner=%s".formatted(
                            skillId,
                            jobId,
                            owner,
                            registeredJobOwner
                    )
            );
        }
    }

}
