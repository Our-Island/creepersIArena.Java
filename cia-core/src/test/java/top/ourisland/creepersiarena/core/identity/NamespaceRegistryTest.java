package top.ourisland.creepersiarena.core.identity;

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
        return new RegistrationOwner(ExtensionId.parse(extensionId), CiaNamespace.parse(namespace));
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

}
