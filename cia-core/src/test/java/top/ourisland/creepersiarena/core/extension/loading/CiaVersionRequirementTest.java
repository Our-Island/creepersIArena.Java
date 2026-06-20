package top.ourisland.creepersiarena.core.extension.loading;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CiaVersionRequirementTest {

    @Test
    void acceptsExactSnapshotVersionOnly() {
        var requirement = CiaVersionRequirement.parse("0.1.0-SNAPSHOT");

        assertTrue(requirement.accepts("0.1.0-SNAPSHOT"));
        assertFalse(requirement.accepts("0.1.0"));
        assertFalse(requirement.accepts("0.1.1-SNAPSHOT"));
    }

    @Test
    void evaluatesInclusiveAndExclusiveRangeBounds() {
        var requirement = CiaVersionRequirement.parse("[0.1.0,0.2.0)");

        assertTrue(requirement.accepts("0.1.0"));
        assertTrue(requirement.accepts("0.1.5"));
        assertFalse(requirement.accepts("0.2.0"));
        assertFalse(requirement.accepts("0.0.9"));
    }

    @Test
    void rejectsMalformedExpressions() {
        assertThrows(IllegalArgumentException.class, () -> CiaVersionRequirement.parse(""));
        assertThrows(IllegalArgumentException.class, () -> CiaVersionRequirement.parse("[0.1.0]"));
        assertThrows(IllegalArgumentException.class, () -> CiaVersionRequirement.parse("[0.2.0,0.1.0]"));
    }

}
