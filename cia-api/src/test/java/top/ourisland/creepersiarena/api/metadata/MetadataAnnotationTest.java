package top.ourisland.creepersiarena.api.metadata;

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

import static org.junit.jupiter.api.Assertions.*;

class MetadataAnnotationTest {

    @Test
    void readsJobModeAndSkillAnnotations() {
        var job = JobMetadata.of(TestJob.class);
        var mode = ModeMetadata.of(TestMode.class);
        var skill = SkillMetadata.of(TestSkill.class);

        assertEquals("custom:job", job.id().id());
        assertFalse(job.enabledByDefault());
        assertEquals("custom:mode", mode.id().id());
        assertFalse(mode.enabledByDefault());
        assertEquals("custom:job.primary", skill.id());
        assertEquals("custom:job", skill.job().id());
        assertEquals(SkillType.ACTIVE, skill.type());
        assertEquals(3, skill.slot());
        assertEquals(12, skill.defaultCooldown());
    }

    @Test
    void missingAnnotationsFailFast() {
        assertThrows(IllegalStateException.class, () -> JobMetadata.of(UnannotatedJob.class));
        assertThrows(IllegalStateException.class, () -> ModeMetadata.of(UnannotatedMode.class));
        assertThrows(IllegalStateException.class, () -> SkillMetadata.of(UnannotatedSkill.class));
    }

    @CiaJobDef(id = "custom:job", enabledByDefault = false)
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

    private static final class UnannotatedJob extends TestJobBase {

    }

    private abstract static class TestJobBase implements IJob {

        @Override
        public ItemStack display() {
            return null;
        }

        @Override
        public ItemStack[] armorTemplate() {
            return new ItemStack[4];
        }

    }

    @CiaModeDef(id = "custom:mode", enabledByDefault = false)
    private static final class TestMode implements IGameMode {

        @Override
        public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
            return new ModeLogic(null, null);
        }

    }

    private static final class UnannotatedMode implements IGameMode {

        @Override
        public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
            return new ModeLogic(null, null);
        }

    }

    @CiaSkillDef(
            id = "custom:job.primary",
            job = "custom:job",
            type = SkillType.ACTIVE,
            slot = 3,
            defaultCooldown = 12
    )
    private static final class TestSkill extends TestSkillBase {

    }

    private static final class UnannotatedSkill extends TestSkillBase {

    }

    private abstract static class TestSkillBase implements ISkillDefinition {

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

}
