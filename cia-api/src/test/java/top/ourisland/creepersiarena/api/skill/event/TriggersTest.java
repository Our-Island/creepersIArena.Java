package top.ourisland.creepersiarena.api.skill.event;

import org.bukkit.event.block.Action;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.skill.event.impl.InteractEvent;
import top.ourisland.creepersiarena.api.skill.event.impl.TickEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriggersTest {

    @Test
    void interactionTriggersMatchOnlyTheirSide() {
        var rightClick = new SkillContext(
                null,
                null,
                new InteractEvent(Action.RIGHT_CLICK_AIR, true),
                null,
                null,
                0,
                null
        );
        var leftClick = new SkillContext(
                null,
                null,
                new InteractEvent(Action.LEFT_CLICK_BLOCK, true),
                null,
                null,
                0,
                null
        );

        assertTrue(Triggers.interactRightClick().matches(rightClick));
        assertFalse(Triggers.interactRightClick().matches(leftClick));
        assertTrue(Triggers.interactLeftClick().matches(leftClick));
        assertFalse(Triggers.interactLeftClick().matches(rightClick));
    }

    @Test
    void tickTriggerRequiresPositivePeriodAndExactMultiple() {
        var tick20 = new SkillContext(
                null,
                null,
                new TickEvent(20),
                null,
                null,
                20,
                null
        );
        var tick21 = new SkillContext(
                null,
                null,
                new TickEvent(21),
                null,
                null,
                21,
                null
        );

        assertTrue(Triggers.tickEvery(10).matches(tick20));
        assertFalse(Triggers.tickEvery(10).matches(tick21));
        assertFalse(Triggers.tickEvery(0).matches(tick20));
        assertFalse(Triggers.tickEvery(-5).matches(tick20));
    }

}
