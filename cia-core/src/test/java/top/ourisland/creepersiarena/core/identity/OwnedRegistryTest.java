package top.ourisland.creepersiarena.core.identity;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.JobId;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
        return RegistrationOwnerAuthority.issue(ExtensionId.parse(extensionId), CiaNamespace.parse(namespace));
    }

    @Test
    void validatesDuplicateBeforeRunningInitializer() {
        var namespaces = new NamespaceRegistry();
        var owner = owner("sample-extension", "sample");
        namespaces.claim(owner);
        var registry = new OwnedRegistry<JobId, String>(namespaces);
        var id = JobId.parse("sample:warrior");
        registry.register(owner, id, "existing");

        var initialized = new AtomicBoolean(false);
        assertThrows(DuplicateRegistrationException.class, () -> registry.registerInitialized(
                owner,
                id,
                "replacement",
                _ -> initialized.set(true)
        ));

        assertFalse(initialized.get());
        assertEquals("existing", registry.get(id).value());
    }

    @Test
    void failedInitializerDoesNotCommitRegistration() {
        var namespaces = new NamespaceRegistry();
        var owner = owner("sample-extension", "sample");
        namespaces.claim(owner);
        var registry = new OwnedRegistry<JobId, String>(namespaces);
        var id = JobId.parse("sample:warrior");

        assertThrows(IllegalStateException.class, () -> registry.registerInitialized(
                owner,
                id,
                "candidate",
                _ -> {
                    throw new IllegalStateException("initialization failed");
                }
        ));

        assertNull(registry.get(id));
    }

    @Test
    void batchValidationRejectsDuplicatesBeforeAnyInitializerRuns() {
        var namespaces = new NamespaceRegistry();
        var owner = owner("sample-extension", "sample");
        namespaces.claim(owner);
        var registry = new OwnedRegistry<JobId, String>(namespaces);
        var initialized = new AtomicInteger();
        var id = JobId.parse("sample:warrior");

        assertThrows(DuplicateRegistrationException.class, () -> registry.registerAllInitialized(
                owner,
                List.of(
                        new OwnedRegistry.Registration<>(id, "first"),
                        new OwnedRegistry.Registration<>(id, "second")
                ),
                _ -> initialized.incrementAndGet()
        ));

        assertEquals(0, initialized.get());
        assertNull(registry.get(id));
    }

    @Test
    void reentrantRegistrationCannotClaimAnInitializingId() {
        var namespaces = new NamespaceRegistry();
        var owner = owner("sample-extension", "sample");
        namespaces.claim(owner);
        var registry = new OwnedRegistry<JobId, String>(namespaces);
        var id = JobId.parse("sample:warrior");

        assertThrows(IllegalStateException.class, () -> registry.registerInitialized(
                owner,
                id,
                "outer",
                _ -> registry.register(owner, id, "reentrant")
        ));

        assertNull(registry.get(id));
    }

    @Test
    void batchInitializerFailureCommitsNoEntries() {
        var namespaces = new NamespaceRegistry();
        var owner = owner("sample-extension", "sample");
        namespaces.claim(owner);
        var registry = new OwnedRegistry<JobId, String>(namespaces);
        var first = JobId.parse("sample:first");
        var second = JobId.parse("sample:second");

        assertThrows(IllegalStateException.class, () -> registry.registerAllInitialized(
                owner,
                List.of(
                        new OwnedRegistry.Registration<>(first, "first"),
                        new OwnedRegistry.Registration<>(second, "second")
                ),
                value -> {
                    if (value.equals("second")) {
                        throw new IllegalStateException("initialization failed");
                    }
                }
        ));

        assertNull(registry.get(first));
        assertNull(registry.get(second));
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
