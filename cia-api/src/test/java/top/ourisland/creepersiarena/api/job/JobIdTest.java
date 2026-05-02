package top.ourisland.creepersiarena.api.job;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobIdTest {

    @Test
    void normalizesCachesAndExtractsPath() {
        var id = JobId.of("  CIA:Creeper  ");

        assertSame(id, JobId.of("cia:creeper"));
        assertEquals("cia:creeper", id.id());
        assertEquals("creeper", id.path());
        assertEquals("cia:creeper", id.toString());
    }

    @Test
    void fromIdReturnsNullForBlankInput() {
        assertNull(JobId.fromId(null));
        assertNull(JobId.fromId("   "));
    }

    @Test
    void ofRejectsBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> JobId.of("\t"));
    }

}
