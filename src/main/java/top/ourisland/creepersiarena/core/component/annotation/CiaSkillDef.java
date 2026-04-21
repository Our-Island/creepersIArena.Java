package top.ourisland.creepersiarena.core.component.annotation;

import top.ourisland.creepersiarena.job.skill.SkillType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CiaSkillDef {

    String id();

    String job();

    SkillType type();

    int slot();

    int defaultCooldown() default 0;

}
