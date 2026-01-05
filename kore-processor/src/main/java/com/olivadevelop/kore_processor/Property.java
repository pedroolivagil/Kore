package com.olivadevelop.kore_processor;

import java.util.Objects;

public class Property {
    private final Integer order;
    private final String type;
    private final String property;
    public Property(Integer order, String type, String property) {
        this.order = order;
        this.type = type;
        this.property = property;
    }
    public Integer getOrder() { return order; }
    public String getType() { return type; }
    public String getProperty() { return property; }
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Property property1 = (Property) o;
        return Objects.equals(order, property1.order) && Objects.equals(type, property1.type) && Objects.equals(property, property1.property);
    }
    @Override
    public int hashCode() {
        return Objects.hash(order, type, property);
    }
}
