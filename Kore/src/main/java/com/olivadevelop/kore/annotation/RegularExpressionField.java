package com.olivadevelop.kore.annotation;

import com.olivadevelop.kore.component.KoreComponentView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegularExpressionField {
    String value();
    boolean mandatory() default true;
    boolean immediateValidation() default true;
    int maxLength() default 50;
    int minLength() default 0;
    int maxLines() default 1;
}
