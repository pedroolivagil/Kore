package com.olivadevelop.kore_processor.component.attribute;

public enum AttributeFormat {
    BOOLEAN("boolean"),
    STRING("string"),
    INTEGER("integer"),
    FLOAT("float"),
    DIMENSION("dimension"),
    COLOR("color"),
    REFERENCE("reference"),
    ENUM("enum");
    private final String xmlName;
    AttributeFormat(String xmlName) { this.xmlName = xmlName; }
    public String getXmlName() { return xmlName; }
    public static AttributeFormat fromXml(String value) {
        for (AttributeFormat f : values()) { if (f.xmlName.equalsIgnoreCase(value)) { return f; } }
        throw new IllegalArgumentException("Unknown attr format: " + value);
    }
}
