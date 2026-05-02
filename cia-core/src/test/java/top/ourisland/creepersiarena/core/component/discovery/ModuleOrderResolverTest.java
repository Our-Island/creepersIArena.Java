package top.ourisland.creepersiarena.core.component.discovery;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModuleOrderResolverTest {

    @Test
    void sortsByOrderThenNameAndHonorsAfterDependencies() {
        var resolver = new ModuleOrderResolver();
        var sorted = resolver.sort(List.of(new LateModule(), new BaseModule(), new MiddleModule()));

        assertInstanceOf(BaseModule.class, sorted.get(0));
        assertInstanceOf(MiddleModule.class, sorted.get(1));
        assertInstanceOf(LateModule.class, sorted.get(2));
    }

    @Test
    void detectsCircularDependencies() {
        var resolver = new ModuleOrderResolver();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> resolver.sort(List.of(new CycleA(), new CycleB()))
        );

        assertTrue(ex.getMessage().contains("Circular bootstrap module dependency"));
    }

    @CiaBootstrapModule(name = "base", order = 10)
    private static final class BaseModule implements IBootstrapModule {

    }

    @CiaBootstrapModule(name = "middle", order = 0, after = BaseModule.class)
    private static final class MiddleModule implements IBootstrapModule {

    }

    @CiaBootstrapModule(name = "late", order = 0, after = MiddleModule.class)
    private static final class LateModule implements IBootstrapModule {

    }

    @CiaBootstrapModule(name = "cycle-a", after = CycleB.class)
    private static final class CycleA implements IBootstrapModule {

    }

    @CiaBootstrapModule(name = "cycle-b", after = CycleA.class)
    private static final class CycleB implements IBootstrapModule {

    }

}
