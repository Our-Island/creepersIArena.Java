package top.ourisland.creepersiarena.api.job;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobIdTest {

    @Test
    void preservesStrictNamespacedIdentity() {
        var id = JobId.parse("cia:creeper");

        assertEquals(JobId.parse("cia:creeper"), id);
        assertNotSame(JobId.parse("cia:creeper"), id);
        assertEquals("cia", id.namespace().value());
        assertEquals("creeper", id.path().value());
        assertEquals("cia:creeper", id.asString());
        assertEquals("cia:creeper", id.toString());
    }

    @Test
    void rejectsBlankBareUppercaseAndNormalizedInputs() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> JobId.parse("")),
                () -> assertThrows(IllegalArgumentException.class, () -> JobId.parse("creeper")),
                () -> assertThrows(IllegalArgumentException.class, () -> JobId.parse(" CIA:Creeper ")),
                () -> assertThrows(IllegalArgumentException.class, () -> JobId.parse("cia:creeper.skill"))
        );
    }

}
