package com.olivadevelop.kore.component.attribute;

import com.olivadevelop.kore.attributtes.KoreComponentViewAttribute;
import com.olivadevelop.kore_annotations.GenerateXMLAttributes;

import java.util.EnumMap;

@GenerateXMLAttributes(styleable = "KoreComponentView")
public class KoreAttributes {
    private final EnumMap<KoreComponentViewAttribute, Object> values = new EnumMap<>(KoreComponentViewAttribute.class);

    public <T> void set(KoreComponentViewAttribute attr, T value) {
        values.put(attr, value);
    }
    @SuppressWarnings("unchecked")
    public <T> T get(KoreComponentViewAttribute attr) {
        return (T) values.getOrDefault(attr, attr.getType());
    }
    public boolean has(KoreComponentViewAttribute attr) {
        return values.containsKey(attr);
    }
}
