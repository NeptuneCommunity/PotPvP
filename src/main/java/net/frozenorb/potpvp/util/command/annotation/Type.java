package net.frozenorb.potpvp.util.command.annotation;

import net.frozenorb.potpvp.util.command.data.ParameterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.PARAMETER})
public @interface Type {
    Class<? extends ParameterType> value();
}

