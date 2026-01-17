package top.ourisland.creepersiarena.job.skill.event;

@FunctionalInterface
public interface Trigger {
    boolean matches(SkillContext ctx);
}
