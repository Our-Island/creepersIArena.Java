package top.ourisland.creepersiarena.defaultcontent;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.job.JobId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultJobIdsTest {

    @Test
    void exposesDefaultJobIdsOutsideCoreApi() {
        assertEquals(DefaultJobIds.CREEPER, JobId.parse("cia:creeper"));
        assertEquals("moison", DefaultJobIds.MOISON.path().value());
        assertEquals("cia:ysahan", DefaultJobIds.YSAHAN.asString());
    }

}
