package top.ourisland.creepersiarena.core.job;

import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobManagerTest {

    @Test
    void resolvesOnlyStrictTypedJobIdsAndKeepsOwner() {
        var namespaces = new NamespaceRegistry();
        var owner = new RegistrationOwner(ExtensionId.parse("default-content"), CiaNamespace.parse("cia"));
        namespaces.claim(owner);
        var manager = new JobManager(namespaces);
        var job = new CreeperLikeJob();

        manager.register(owner, job);

        var id = JobId.parse("cia:creeper");
        assertSame(job, manager.getJob(id));
        assertEquals(owner, manager.ownerOf(id));
        assertEquals(List.of(id), manager.getAllJobIds());
        assertThrows(IllegalArgumentException.class, () -> JobId.parse("creeper"));
    }

    @Test
    void duplicateRegistrationFailsInsteadOfReplacing() {
        var namespaces = new NamespaceRegistry();
        var owner = new RegistrationOwner(ExtensionId.parse("default-content"), CiaNamespace.parse("cia"));
        namespaces.claim(owner);
        var manager = new JobManager(namespaces);
        manager.register(owner, new CreeperLikeJob());

        assertThrows(RuntimeException.class, () -> manager.register(owner, new CreeperLikeJob()));
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
