package top.ourisland.creepersiarena.core.component.annotation;

import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CiaBootstrapModule {

    String name();

    int order() default 0;

    Class<? extends IBootstrapModule>[] after() default {};

}
