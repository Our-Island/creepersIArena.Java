package top.ourisland.creepersiarena.api.region;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundsTest {

    @Test
    void bounds2dNormalizesCoordinateOrder() {
        var bounds = Bounds2D.of(10, 30, -5, 7);

        assertEquals(-5, bounds.minX());
        assertEquals(10, bounds.maxX());
        assertEquals(7, bounds.minZ());
        assertEquals(30, bounds.maxZ());
        assertFalse(bounds.hasY());
    }

    @Test
    void bounds3dNormalizesCoordinateOrderAndReportsY() {
        var bounds = Bounds3D.of(5, 100, -8, -2, 70, 20);

        assertEquals(-2, bounds.minX());
        assertEquals(5, bounds.maxX());
        assertEquals(70, bounds.minY());
        assertEquals(100, bounds.maxY());
        assertEquals(-8, bounds.minZ());
        assertEquals(20, bounds.maxZ());
        assertTrue(bounds.hasY());
    }

}
