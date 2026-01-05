package com.olivadevelop.kore.util;

import java.util.List;

import lombok.Getter;

@Getter
public final class TypeDescriptor {
    private final Class<?> wrapper;      // Contenedor
    private final List<Class<?>> arguments; // Tipos genéricos
    public TypeDescriptor(Class<?> wrapper, List<Class<?>> arguments) {
        this.wrapper = wrapper;
        this.arguments = arguments;
    }
    public boolean isParameterized() {
        return wrapper != null && !arguments.isEmpty();
    }
}
