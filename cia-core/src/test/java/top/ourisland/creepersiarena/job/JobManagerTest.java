package top.ourisland.creepersiarena.job;

import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.job.JobId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class JobManagerTest {

    @Test
    void resolvesNamespacedAndLegacyPlainJobIds() {
        var manager = new JobManager();
        var job = new CreeperLikeJob();

        manager.register("default-content", job);

        assertSame(job, manager.getJob("cia:creeper"));
        assertSame(job, manager.getJob("creeper"));
        assertSame(job, manager.getJob(JobId.of("cia:creeper")));
        assertEquals("default-content", manager.ownerOf(JobId.of("creeper")));
        assertEquals("cia:creeper", manager.getAllJobIds().getFirst());
    }

    @CiaJobDef(id = "cia:creeper")
    private static final class CreeperLikeJob implements IJob {

        @Override
        public ItemStack display() {
            return null;
        }

        @Override
        public ItemStack[] armorTemplate() {
            return new ItemStack[4];
        }

    }

}
