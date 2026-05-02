package top.ourisland.creepersiarena.core.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServiceRegistryTest {

    @Test
    void storesReplacesAndCastsServicesByClassKey() {
        var registry = new ServiceRegistry();

        registry.put(String.class, "first");
        registry.put(Integer.class, 7);
        registry.put(String.class, "second");

        assertEquals("second", registry.get(String.class));
        assertEquals(7, registry.require(Integer.class));
        assertNull(registry.get(Double.class));
    }

}
