package com.olivadevelop.kore.annotation;

import com.olivadevelop.kore.component.KoreComponentView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomViewRender {
    Class<? extends KoreComponentView<?>> value();
    Class<?> converTo();
}
