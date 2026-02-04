package com.olivadevelop.kore_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateXMLAttributes {

    String xmlProjectPath();
    /**
     * Nombre del declare-styleable
     * Ej: "KoreComponentView"
     */
    String styleable();

    /**
     * Ruta relativa al attrs.xml
     * Ej: "res/values/attrs.xml"
     */
    String xmlPath() default "\\src\\main\\res\\values\\attrs.xml";

    /**
     * Paquete destino del código generado
     */
    String targetPackage() default "com.olivadevelop.kore.attributtes";

    /**
     * Prefijo opcional para enums/clases
     */
    String prefix() default "";
}
