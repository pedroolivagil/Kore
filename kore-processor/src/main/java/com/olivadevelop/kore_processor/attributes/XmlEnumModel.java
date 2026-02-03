package com.olivadevelop.kore_processor.attributes;

import java.util.List;
import java.util.Objects;

public class XmlEnumModel {
    private String enumName; // TextStyle
    private List<XmlEnumValue> values;
    public String getEnumName() {
        return enumName;
    }
    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }
    public List<XmlEnumValue> getValues() {
        return values;
    }
    public void setValues(List<XmlEnumValue> values) {
        this.values = values;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        XmlEnumModel that = (XmlEnumModel) o;
        return Objects.equals(enumName, that.enumName) && Objects.equals(values, that.values);
    }
    @Override
    public int hashCode() {
        return Objects.hash(enumName, values);
    }
}
