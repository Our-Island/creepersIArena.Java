package top.ourisland.creepersiarena.job.skill;

import top.ourisland.creepersiarena.core.component.metadata.SkillMetadata;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;

import java.util.List;

public interface ISkillDefinition {

    default String id() {
        return SkillMetadata.of(getClass()).id();
    }

    default String jobId() {
        return SkillMetadata.of(getClass()).job().id();
    }

    default SkillType type() {
        return SkillMetadata.of(getClass()).type();
    }

    default int uiSlot() {
        return SkillMetadata.of(getClass()).slot();
    }

    /**
     * The default cooldown second of the skill. Could be changed by the skill.yml.
     *
     * @return the cooldown seconds
     */
    default int cooldownSeconds() {
        return SkillMetadata.of(getClass()).defaultCooldown();
    }

    List<ITrigger> triggers();

    ISkillIcon icon();

    ISkillExecutor executor();

}
