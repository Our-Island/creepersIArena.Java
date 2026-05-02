package top.ourisland.creepersiarena.api.skill.event;

@FunctionalInterface
public interface ITrigger {

    boolean matches(SkillContext ctx);

}
