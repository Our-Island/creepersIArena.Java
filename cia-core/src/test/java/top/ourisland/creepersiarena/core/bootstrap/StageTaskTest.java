package top.ourisland.creepersiarena.core.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StageTaskTest {

    @Test
    void storesMessagesAndExecutableAction() {
        var counter = new AtomicInteger();
        var task = StageTask.of(counter::incrementAndGet, "begin", "end");

        assertEquals("begin", task.beginMessage());
        assertEquals("end", task.endMessage());
        task.action().run();
        assertEquals(1, counter.get());
    }

}
