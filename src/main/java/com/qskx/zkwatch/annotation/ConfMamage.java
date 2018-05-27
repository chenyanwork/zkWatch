package com.qskx.zkwatch.annotation;

import com.sun.javafx.sg.prism.NodeEffectInput;
import org.springframework.beans.factory.annotation.Value;

import java.lang.annotation.*;

/**
 * @author 111111
 * @date 2018-05-27 22:14
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfMamage {

    String value();

    String defaultValue() default "";

    boolean callback() default true;
}
