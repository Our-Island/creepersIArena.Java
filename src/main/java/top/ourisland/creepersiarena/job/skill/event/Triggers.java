package top.ourisland.creepersiarena.job.skill.event;

import top.ourisland.creepersiarena.job.skill.event.impl.InteractEvent;
import top.ourisland.creepersiarena.job.skill.event.impl.TickEvent;

public final class Triggers {
    private Triggers() {
    }

    public static ITrigger interactRightClick() {
        return ctx -> (ctx.event() instanceof InteractEvent ie)
                && ie.action().isRightClick();
    }

    public static ITrigger interactLeftClick() {
        return ctx -> (ctx.event() instanceof InteractEvent ie)
                && ie.action().isLeftClick();
    }

    public static ITrigger tickEvery(long periodTicks) {
        return ctx -> (ctx.event() instanceof TickEvent(long nowTick))
                && periodTicks > 0 && (nowTick % periodTicks == 0);
    }
}
