package top.ourisland.creepersiarena.job.skill.runtime;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillRegistryTest {

    @Test
    void groupsSortsAndReplacesSkillsPerJob() {
        var registry = new SkillRegistry(new PlayerSessionStore());

        registry.register("extension-a", new LateSkill());
        registry.register("extension-b", new EarlySkill());
        registry.register("extension-c", new OtherJobSkill());

        assertEquals(
                List.of("test:job.early", "test:job.late"),
                registry.skillsOf(JobId.of("test:job")).stream()
                        .map(ISkillDefinition::id)
                        .toList()
        );
        assertEquals("extension-b", registry.ownerOf("test:job.early"));
        assertEquals(
                List.of("test:other.only"),
                registry.skillsOf(JobId.of("test:other")).stream()
                        .map(ISkillDefinition::id)
                        .toList()
        );

        registry.register("replacement", new EarlySkill());

        assertEquals("replacement", registry.ownerOf("test:job.early"));
        assertEquals(3, registry.registeredSkills().size());
    }

    private abstract static class BaseSkill implements ISkillDefinition {

        @Override
        public List<ITrigger> triggers() {
            return List.of();
        }

        @Override
        public ISkillIcon icon() {
            return _ -> null;
        }

        @Override
        public ISkillExecutor executor() {
            return (_, _) -> {
            };
        }

    }

    @CiaSkillDef(
            id = "test:job.late",
            job = "test:job",
            type = SkillType.ACTIVE,
            slot = 8
    )
    private static final class LateSkill extends BaseSkill {

    }

    @CiaSkillDef(
            id = "test:job.early",
            job = "test:job",
            type = SkillType.ACTIVE,
            slot = 1
    )
    private static final class EarlySkill extends BaseSkill {

    }

    @CiaSkillDef(
            id = "test:other.only",
            job = "test:other",
            type = SkillType.ACTIVE,
            slot = 0
    )
    private static final class OtherJobSkill extends BaseSkill {

    }

}
