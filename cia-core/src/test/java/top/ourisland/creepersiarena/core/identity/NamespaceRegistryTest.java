package top.ourisland.creepersiarena.core.identity;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceRegistryTest {

    @Test
    void enforcesExclusiveClaimsAndReservedNamespaces() {
        var registry = new NamespaceRegistry();
        var first = owner("first-extension", "shared");
        var second = owner("second-extension", "shared");

        registry.claim(first);
        assertEquals(first, registry.owner(CiaNamespace.parse("shared")));
        assertThrows(IllegalStateException.class, () -> registry.claim(second));
        assertThrows(IllegalArgumentException.class,
                () -> registry.claim(owner("other-extension", "core")));
        assertThrows(IllegalArgumentException.class,
                () -> registry.claim(owner("minecraft-extension", "minecraft")));
    }

    private static RegistrationOwner owner(String extensionId, String namespace) {
        return RegistrationOwnerAuthority.issue(ExtensionId.parse(extensionId), CiaNamespace.parse(namespace));
    }

    @Test
    void rejectsOwnersThatWereNotIssuedByCoreRuntime() {
        var registry = new NamespaceRegistry();
        var fake = new RegistrationOwner() {
            @Override
            public @NonNull ExtensionId extensionId() {
                return ExtensionId.parse("fake-extension");
            }

            @Override
            public @NonNull CiaNamespace namespace() {
                return CiaNamespace.parse("fake");
            }
        };

        assertThrows(SecurityException.class, () -> registry.claim(fake));
        assertThrows(SecurityException.class, () -> registry.release(fake));
        assertThrows(SecurityException.class, () -> registry.requireOwnership(fake, CiaNamespace.parse("fake")));
    }

    @Test
    void releaseOnlyRemovesTheMatchingOwner() {
        var registry = new NamespaceRegistry();
        var owner = owner("sample-extension", "sample");
        registry.claim(owner);

        registry.release(owner("different-extension", "sample"));
        assertEquals(owner, registry.owner(CiaNamespace.parse("sample")));

        registry.release(owner);
        assertNull(registry.owner(CiaNamespace.parse("sample")));
    }

    @Test
    void textualIdentityCannotForgeAnExistingClaim() {
        var registry = new NamespaceRegistry();
        var issued = owner("sample-extension", "sample");
        var forged = owner("sample-extension", "sample");
        registry.claim(issued);

        assertNotSame(issued, forged);
        assertThrows(IllegalStateException.class, () -> registry.claim(forged));
        assertThrows(
                IllegalArgumentException.class,
                () -> registry.requireOwnership(forged, CiaNamespace.parse("sample"))
        );

        registry.release(forged);
        assertSame(issued, registry.owner(CiaNamespace.parse("sample")));
    }

}
