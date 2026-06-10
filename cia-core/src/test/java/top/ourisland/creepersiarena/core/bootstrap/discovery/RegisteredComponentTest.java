package top.ourisland.creepersiarena.core.bootstrap.discovery;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisteredComponentTest {

    @Test
    void normalizesOwnerIdsAndDefaultsBlankOwnersToCore() {
        assertEquals("core", RegisteredComponent.normalizeOwnerId(null));
        assertEquals("core", RegisteredComponent.normalizeOwnerId("   "));
        assertEquals("custom-extension", RegisteredComponent.normalizeOwnerId(" Custom-Extension "));

        var component = new RegisteredComponent<>(" My.Extension ", "key", "value");

        assertEquals("my.extension", component.ownerId());
        assertEquals("key", component.key());
        assertEquals("value", component.value());
    }

    @Test
    void rejectsNullKeyAndValue() {
        assertThrows(
                NullPointerException.class,
                () -> new RegisteredComponent<>("owner", null, "value")
        );
        assertThrows(
                NullPointerException.class,
                () -> new RegisteredComponent<>("owner", "key", null)
        );
    }

}
