package com.olivadevelop.kore.component;

import java.lang.annotation.Annotation;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentProperty {
    private String property;
    private List<? extends Annotation> annotations;
    private Class<?> componentClass;
    private int order;
    private int group;
}