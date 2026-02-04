package com.olivadevelop.kore_processor.attributes;

import java.util.Objects;
import java.util.Set;

public class XmlAttrModel {
    private String name;               // mandatory
    private Set<AttributeFormat> formats;   // boolean, enum, string...
    private XmlEnumModel enumModel;    // null si no es enum
    public AttributeFormat primaryType() {
        if (formats.contains(AttributeFormat.ENUM)) { return AttributeFormat.ENUM; }
        if (formats.contains(AttributeFormat.STRING)) { return AttributeFormat.STRING; }
        if (formats.contains(AttributeFormat.BOOLEAN)) { return AttributeFormat.BOOLEAN; }
        if (formats.contains(AttributeFormat.INTEGER)) { return AttributeFormat.INTEGER; }
        if (formats.contains(AttributeFormat.FLOAT)) { return AttributeFormat.FLOAT; }
        if (formats.contains(AttributeFormat.DIMENSION)) { return AttributeFormat.DIMENSION; }
        if (formats.contains(AttributeFormat.COLOR)) { return AttributeFormat.COLOR; }
        if (formats.contains(AttributeFormat.REFERENCE)) { return AttributeFormat.REFERENCE; }
        throw new IllegalStateException("No primary type for attr: " + name);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Set<AttributeFormat> getFormats() {
        return formats;
    }
    public void setFormats(Set<AttributeFormat> formats) {
        this.formats = formats;
    }
    public XmlEnumModel getEnumModel() {
        return enumModel;
    }
    public void setEnumModel(XmlEnumModel enumModel) {
        this.enumModel = enumModel;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        XmlAttrModel that = (XmlAttrModel) o;
        return Objects.equals(name, that.name) && Objects.equals(formats, that.formats) && Objects.equals(enumModel, that.enumModel);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, formats, enumModel);
    }
}