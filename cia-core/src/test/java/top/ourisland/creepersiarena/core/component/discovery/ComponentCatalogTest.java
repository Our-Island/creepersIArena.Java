package top.ourisland.creepersiarena.core.component.discovery;

import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.game.mode.ModeLogic;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentCatalogTest {

    @Test
    void tracksOwnersForRegisteredJobsSkillsAndModes() {
        var catalog = new ComponentCatalog();
        var job = new TestJob();
        var skill = new TestSkill();
        var mode = new TestMode();

        catalog.registerJob(" Ext-One ", job);
        catalog.registerSkill(" Ext-One ", skill);
        catalog.registerMode(" Ext-One ", mode);

        assertEquals(List.of(job), catalog.jobs());
        assertEquals(List.of(skill), catalog.skills());
        assertEquals(List.of(mode), catalog.modes());
        assertEquals("ext-one", catalog.ownerOfJob("test:job"));
        assertEquals("ext-one", catalog.ownerOfSkill("test:job.primary"));
        assertEquals("ext-one", catalog.ownerOfMode("test:mode"));
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
            id = "test:job.primary",
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
