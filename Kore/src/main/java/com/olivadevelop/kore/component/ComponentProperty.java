package com.olivadevelop.kore.component;

import java.lang.annotation.Annotation;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComponentProperty {
    private String property;
    private List<? extends Annotation> annotations;
    private Class<?> componentClass;
    private int order;
    public ComponentProperty() { }
    public ComponentProperty(String property, List<? extends Annotation> annotations, Class<?> componentClass, int order) {
        this.property = property;
        this.annotations = annotations;
        this.componentClass = componentClass;
        this.order = order;
    }
}