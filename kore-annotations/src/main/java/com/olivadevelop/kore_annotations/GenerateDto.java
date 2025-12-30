package com.olivadevelop.kore_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateDto {
    /**
     * Package del DTO generado
     * Ej: "com.app.dto"
     */
    String dtoPackage();

    /**
     * Sufijo del DTO
     * User → UserDto
     */
    String suffix() default "Dto";
    /**
     * Nombre del DTO
     * Ej: "User"
     */
    String name() default "";
    /**
     * Prefijo de las propiedades del DTO
     * Ej: "user_"
     */
    String propertyPrefix() default "";

    /**
     * Añadir Lombok @Data
     */
    boolean data() default true;

    /**
     * Añadir Lombok @Builder
     */
    boolean builder() default true;
    boolean usePrimitives() default true;
}
