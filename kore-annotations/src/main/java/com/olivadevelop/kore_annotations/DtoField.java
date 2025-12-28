package com.olivadevelop.kore_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DtoField {
    /**
     * Nombre alternativo en el DTO
     */
    String name() default "";

    /**
     * Ignorar este campo en el DTO
     */
    boolean ignore() default false;

    /**
     * Tipo explícito del DTO.
     * (NO lo usamos aún, pero queda preparado)
     */
    Class<?> type() default void.class;
}
