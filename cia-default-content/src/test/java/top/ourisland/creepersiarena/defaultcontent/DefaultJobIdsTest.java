package top.ourisland.creepersiarena.defaultcontent;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.job.JobId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultJobIdsTest {

    @Test
    void exposesDefaultJobIdsOutsideCoreApi() {
        assertSame(DefaultJobIds.CREEPER, JobId.of("cia:creeper"));
        assertEquals("moison", DefaultJobIds.MOISON.path());
        assertEquals("cia:ysahan", DefaultJobIds.YSAHAN.id());
    }

}
