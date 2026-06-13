package top.ourisland.creepersiarena.core.bootstrap.discovery;

import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.game.mode.ModeLogic;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.*;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentCatalogTest {

    @Test
    void tracksOwnersForRegisteredJobsSkillsAndModes() {
        var namespaces = new NamespaceRegistry();
        var owner = new RegistrationOwner(ExtensionId.parse("ext-one"), CiaNamespace.parse("test"));
        namespaces.claim(owner);
        var catalog = new ComponentCatalog(namespaces);
        var job = new TestJob();
        var skill = new TestSkill();
        var mode = new TestMode();

        catalog.registerJob(owner, job);
        catalog.registerSkill(owner, skill);
        catalog.registerMode(owner, mode);

        assertEquals(List.of(job), catalog.jobs());
        assertEquals(List.of(skill), catalog.skills());
        assertEquals(List.of(mode), catalog.modes());
        assertEquals(owner, catalog.ownerOfJob(JobId.parse("test:job")));
        assertEquals(owner, catalog.ownerOfSkill(SkillId.parse("test:job/primary")));
        assertEquals(owner, catalog.ownerOfMode(GameModeId.parse("test:mode")));
    }

    @CiaJobDef(id = "test:job")
    private static final class TestJob implements IJob {

        @Override
        public ItemStack display() {
            return null;
        }

        @Override
        public ItemStack[] armorTemplate() {
            return new ItemStack[4];
        }

    }

    @CiaSkillDef(
            id = "test:job/primary",
            job = "test:job",
            type = SkillType.ACTIVE,
            slot = 0
    )
    private static final class TestSkill implements ISkillDefinition {

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

    @CiaModeDef(id = "test:mode")
    private static final class TestMode implements IGameMode {

        @Override
        public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
            return new ModeLogic(null, null);
        }

    }

}
