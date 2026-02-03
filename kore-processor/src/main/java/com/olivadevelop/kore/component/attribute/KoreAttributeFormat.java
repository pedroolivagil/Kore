package com.olivadevelop.kore.component.attribute;

public enum KoreAttributeFormat {
    BOOLEAN("boolean"),
    STRING("string"),
    INTEGER("integer"),
    FLOAT("float"),
    DIMENSION("dimension"),
    COLOR("color"),
    REFERENCE("reference"),
    ENUM("enum");
    private final String xmlName;
    KoreAttributeFormat(String xmlName) { this.xmlName = xmlName; }
    public String getXmlName() { return xmlName; }
    public static KoreAttributeFormat fromXml(String value) {
        for (KoreAttributeFormat f : values()) { if (f.xmlName.equalsIgnoreCase(value)) { return f; } }
        throw new IllegalArgumentException("Unknown attr format: " + value);
    }
}
