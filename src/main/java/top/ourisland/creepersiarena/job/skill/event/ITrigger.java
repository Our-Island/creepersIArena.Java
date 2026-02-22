package top.ourisland.creepersiarena.job.skill.event;

@FunctionalInterface
public interface ITrigger {
    boolean matches(SkillContext ctx);
}
