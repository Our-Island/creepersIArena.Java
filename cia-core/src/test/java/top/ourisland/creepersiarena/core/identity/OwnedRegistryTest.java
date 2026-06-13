package top.ourisland.creepersiarena.core.identity;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.JobId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OwnedRegistryTest {

    @Test
    void checksOwnershipAndRejectsDuplicateRegistrations() {
        var namespaces = new NamespaceRegistry();
        var owner = owner("sample-extension", "sample");
        var foreign = owner("foreign-extension", "foreign");
        namespaces.claim(owner);
        namespaces.claim(foreign);
        var registry = new OwnedRegistry<JobId, String>(namespaces);
        var id = JobId.parse("sample:warrior");

        registry.register(owner, id, "first");

        assertEquals("first", registry.get(id).value());
        assertThrows(DuplicateRegistrationException.class,
                () -> registry.register(owner, id, "second"));
        assertThrows(IllegalArgumentException.class,
                () -> registry.register(foreign, JobId.parse("sample:mage"), "invalid"));
    }

    private static RegistrationOwner owner(String extensionId, String namespace) {
        return new RegistrationOwner(ExtensionId.parse(extensionId), CiaNamespace.parse(namespace));
    }

    @Test
    void replaceOwnerIsAtomicAndCannotOverwriteAnotherOwner() {
        var namespaces = new NamespaceRegistry();
        var alpha = owner("alpha-extension", "alpha");
        var beta = owner("beta-extension", "beta");
        namespaces.claim(alpha);
        namespaces.claim(beta);
        var registry = new OwnedRegistry<JobId, String>(namespaces);
        registry.register(alpha, JobId.parse("alpha:one"), "one");
        registry.register(beta, JobId.parse("beta:kept"), "kept");

        registry.replaceOwner(alpha, List.of(
                new OwnedRegistry.Registration<>(JobId.parse("alpha:two"), "two"),
                new OwnedRegistry.Registration<>(JobId.parse("alpha:three"), "three")
        ));

        assertNull(registry.get(JobId.parse("alpha:one")));
        assertEquals("two", registry.get(JobId.parse("alpha:two")).value());
        assertEquals("kept", registry.get(JobId.parse("beta:kept")).value());

        assertThrows(IllegalArgumentException.class, () -> registry.replaceOwner(alpha, List.of(
                new OwnedRegistry.Registration<>(JobId.parse("beta:kept"), "overwrite")
        )));
        assertEquals("two", registry.get(JobId.parse("alpha:two")).value());
        assertEquals("kept", registry.get(JobId.parse("beta:kept")).value());
    }

}
