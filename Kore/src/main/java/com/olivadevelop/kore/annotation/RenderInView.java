package com.olivadevelop.kore.annotation;

import com.olivadevelop.kore.activity.KoreActivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RenderInView {
    Class<KoreActivity<?, ?>>[] value();
}
