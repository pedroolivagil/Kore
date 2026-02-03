package com.olivadevelop.kore_processor.attributes;

import com.olivadevelop.kore.component.attribute.KoreAttributeFormat;

import java.util.Objects;
import java.util.Set;

public class XmlAttrModel {
    private String name;               // mandatory
    private Set<KoreAttributeFormat> formats;   // boolean, enum, string...
    private XmlEnumModel enumModel;    // null si no es enum
    public KoreAttributeFormat primaryType() {
        if (formats.contains(KoreAttributeFormat.ENUM)) { return KoreAttributeFormat.ENUM; }
        if (formats.contains(KoreAttributeFormat.STRING)) { return KoreAttributeFormat.STRING; }
        if (formats.contains(KoreAttributeFormat.BOOLEAN)) { return KoreAttributeFormat.BOOLEAN; }
        if (formats.contains(KoreAttributeFormat.INTEGER)) { return KoreAttributeFormat.INTEGER; }
        if (formats.contains(KoreAttributeFormat.FLOAT)) { return KoreAttributeFormat.FLOAT; }
        if (formats.contains(KoreAttributeFormat.DIMENSION)) { return KoreAttributeFormat.DIMENSION; }
        if (formats.contains(KoreAttributeFormat.COLOR)) { return KoreAttributeFormat.COLOR; }
        if (formats.contains(KoreAttributeFormat.REFERENCE)) { return KoreAttributeFormat.REFERENCE; }
        throw new IllegalStateException("No primary type for attr: " + name);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Set<KoreAttributeFormat> getFormats() {
        return formats;
    }
    public void setFormats(Set<KoreAttributeFormat> formats) {
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