package top.ourisland.creepersiarena.core.job.skill.runtime;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.core.identity.RegistrationOwnerAuthority;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.*;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkillRegistryTest {

    @Test
    void groupsSortsAndRejectsDuplicateSkills() {
        var namespaces = new NamespaceRegistry();
        var owner = RegistrationOwnerAuthority.issue(ExtensionId.parse("extension-a"), CiaNamespace.parse("test"));
        namespaces.claim(owner);
        var registry = new SkillRegistry(
                new PlayerSessionStore(),
                namespaces,
                jobId -> switch (jobId.asString()) {
                    case "test:job", "test:other" -> owner;
                    default -> null;
                }
        );

        registry.register(owner, new LateSkill());
        registry.register(owner, new EarlySkill());
        registry.register(owner, new OtherJobSkill());

        assertEquals(
                List.of("test:job/early", "test:job/late"),
                registry.skillsOf(JobId.parse("test:job")).stream()
                        .map(skill -> skill.id().asString())
                        .toList()
        );
        assertEquals(owner, registry.ownerOf(SkillId.parse("test:job/early")));
        assertEquals(
                List.of("test:other/only"),
                registry.skillsOf(JobId.parse("test:other")).stream()
                        .map(skill -> skill.id().asString())
                        .toList()
        );

        assertThrows(RuntimeException.class, () -> registry.register(owner, new EarlySkill()));
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
            id = "test:job/late",
            job = "test:job",
            type = SkillType.ACTIVE,
            slot = 8
    )
    private static final class LateSkill extends BaseSkill {

    }

    @CiaSkillDef(
            id = "test:job/early",
            job = "test:job",
            type = SkillType.ACTIVE,
            slot = 1
    )
    private static final class EarlySkill extends BaseSkill {

    }

    @CiaSkillDef(
            id = "test:other/only",
            job = "test:other",
            type = SkillType.ACTIVE,
            slot = 0
    )
    private static final class OtherJobSkill extends BaseSkill {

    }

}
