package net.frozenorb.potpvp.util.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.PARAMETER})
public @interface Param {
    String name();

    String defaultValue() default "";

    String[] tabCompleteFlags() default {};

    boolean wildcard() default false;
}

