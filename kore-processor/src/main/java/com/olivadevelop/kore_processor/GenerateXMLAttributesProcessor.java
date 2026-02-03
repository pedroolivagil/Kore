package com.olivadevelop.kore_processor;

import static org.apache.commons.lang3.StringUtils.capitalize;

import com.google.auto.service.AutoService;
import com.olivadevelop.kore.component.attribute.KoreAttributeFormat;
import com.olivadevelop.kore_annotations.GenerateXMLAttributes;
import com.olivadevelop.kore_processor.attributes.XmlAttrModel;
import com.olivadevelop.kore_processor.attributes.XmlEnumModel;
import com.olivadevelop.kore_processor.attributes.XmlEnumValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions("kore.version")
public class GenerateXMLAttributesProcessor extends AbstractProcessor {
    private Filer filer;
    private String koreVersion;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        koreVersion = env.getOptions().get("kore.version");
        if (koreVersion == null) { koreVersion = "unknown"; }
    }
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(GenerateXMLAttributes.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (javax.lang.model.element.Element e : roundEnv.getElementsAnnotatedWith(GenerateXMLAttributes.class)) {
            GenerateXMLAttributes cfg = e.getAnnotation(GenerateXMLAttributes.class);
            generate(cfg);
        }
        return true;
    }
    private void generate(GenerateXMLAttributes cfg) {
        try {
            Document document = loadXml(cfg.xmlPath());
            Element styleable = findStyleable(document, cfg.styleable());
            if (styleable == null) { return; }
            List<XmlAttrModel> attrs = parseAttributes(styleable);
            generateEnumsIfNeeded(attrs, cfg);
            generateAttrEnum(attrs, cfg);
        } catch (Exception e) {
            throw new RuntimeException("Error generating XML attributes", e);
        }
    }
    private Document loadXml(String xmlPath) throws Exception {
        xmlPath = "D:\\__Proyectos\\Android\\Kore\\kore-android-tester\\src\\main\\res\\values\\attrs.xml";
//        Path projectDir = Paths.get(processingEnv.getOptions().getOrDefault("kapt.kotlin.generated", "")).getParent();
        Path projectDir = Paths.get("").toAbsolutePath();
        Path path = projectDir.resolve(xmlPath).normalize();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(path.toFile());
    }
    private Element findStyleable(Document doc, String name) {
        NodeList nodes = doc.getElementsByTagName("declare-styleable");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (name.equals(el.getAttribute("name"))) { return el; }
        }
        return null;
    }
    private List<XmlAttrModel> parseAttributes(Element styleable) {
        List<XmlAttrModel> attrs = new ArrayList<>();
        NodeList nodes = styleable.getElementsByTagName("attr");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element attr = (Element) nodes.item(i);
            XmlAttrModel model = new XmlAttrModel();
            model.setName(attr.getAttribute("name"));
            model.setFormats(parseFormats(attr));
            if (model.getFormats().contains(KoreAttributeFormat.ENUM)) { model.setEnumModel(parseEnum(attr, model.getName())); }
            attrs.add(model);
        }
        return attrs;
    }
    private Set<KoreAttributeFormat> parseFormats(Element attr) {
        Set<KoreAttributeFormat> formats = new HashSet<>();
        String format = attr.getAttribute("format");
        if (format.isEmpty()) { return formats; }
        for (String f : format.split("\\|")) { formats.add(KoreAttributeFormat.fromXml(f.trim())); }
        return formats;
    }
    private XmlEnumModel parseEnum(Element attr, String attrName) {
        XmlEnumModel model = new XmlEnumModel();
        model.setEnumName(capitalize(attrName));
        model.setValues(new ArrayList<>());
        NodeList enums = attr.getElementsByTagName("enum");
        for (int i = 0; i < enums.getLength(); i++) {
            Element e = (Element) enums.item(i);
            model.getValues().add(new XmlEnumValue(
                    e.getAttribute("name"),
                    Integer.parseInt(e.getAttribute("value"))
            ));
        }
        return model;
    }
    private void generateEnumsIfNeeded(List<XmlAttrModel> attrs, GenerateXMLAttributes cfg) {
        for (XmlAttrModel attr : attrs) { if (attr.getEnumModel() != null) { generateEnum(attr.getEnumModel(), cfg); } }
    }
    private void generateEnum(XmlEnumModel model, GenerateXMLAttributes cfg) {
        String enumName = capitalize(model.getEnumName());
        String pkg = cfg.targetPackage();
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";").append(Params.LINE_BREAK_DOUBLE);
        Set<String> imports = new HashSet<>();
        imports.add(lombok.Getter.class.getCanonicalName());
        imports.add(javax.annotation.processing.Generated.class.getCanonicalName());
        imports.stream().sorted().forEach(i -> sb.append("import ").append(i).append(Params.SEMICOLON).append(Params.LINE_BREAK));
        sb.append("@Getter").append(Params.LINE_BREAK);
        sb.append("@Generated(value = \"")
                .append(getClass().getSimpleName())
                .append("\", date = \"")
                .append(getCurrentDate())
                .append("\")").append(Params.LINE_BREAK);
        sb.append("public enum ").append(enumName).append(" {").append(Params.LINE_BREAK);
        for (XmlEnumValue v : model.getValues()) {
            sb.append("    ")
                    .append(v.getName().toUpperCase())
                    .append("(")
                    .append(v.getValue())
                    .append("),").append(Params.LINE_BREAK);
        }
        sb.append("    ;").append(Params.LINE_BREAK_DOUBLE);
        sb.append("    public final int value;").append(Params.LINE_BREAK);
        sb.append("    ").append(enumName).append("(int value) {").append(Params.LINE_BREAK);
        sb.append("        this.value = value;").append(Params.LINE_BREAK);
        sb.append("    }").append(Params.LINE_BREAK);
        sb.append("}").append(Params.LINE_BREAK);
        writeFile(pkg, enumName, sb.toString());
    }

    private void generateAttrEnum(List<XmlAttrModel> attrs, GenerateXMLAttributes cfg) {
        String className = cfg.prefix() + cfg.styleable() + "Attribute";
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(cfg.targetPackage()).append(Params.SEMICOLON).append(Params.LINE_BREAK_DOUBLE);
        sb.append("import static ").append(com.olivadevelop.kore.component.attribute.KoreAttributeFormat.class.getCanonicalName())
                .append(".*").append(Params.SEMICOLON).append(Params.LINE_BREAK_DOUBLE);
        Set<String> imports = new HashSet<>();
        imports.add(com.olivadevelop.kore.component.attribute.KoreAttributeFormat.class.getCanonicalName());
        imports.add(lombok.Getter.class.getCanonicalName());
        imports.add(javax.annotation.processing.Generated.class.getCanonicalName());
        imports.stream().sorted().forEach(i -> sb.append("import ").append(i).append(Params.SEMICOLON).append(Params.LINE_BREAK));
        sb.append("@Getter").append(Params.LINE_BREAK);
        sb.append("@Generated(value = \"")
                .append(getClass().getSimpleName())
                .append(".class\", date = \"")
                .append(getCurrentDate())
                .append("\")").append(Params.LINE_BREAK);
        sb.append("public enum ").append(className).append(" {").append(Params.LINE_BREAK);
        for (XmlAttrModel attr : attrs) {
            sb.append("    ")
                    .append(attr.getName().toUpperCase())
                    .append("(\"")
                    .append(attr.getName())
                    .append("\", ")
                    .append(attr.primaryType())
                    .append("),").append(Params.LINE_BREAK);
        }
        sb.append(";").append(Params.LINE_BREAK_DOUBLE);
        sb.append("    public final String xmlName;").append(Params.LINE_BREAK);
        sb.append("    public final KoreAttributeFormat type;").append(Params.LINE_BREAK);
        sb.append("    ").append(className)
                .append("(String xmlName, KoreAttributeFormat type) {").append(Params.LINE_BREAK)
                .append("        this.xmlName = xmlName;").append(Params.LINE_BREAK)
                .append("        this.type = type;").append(Params.LINE_BREAK)
                .append("    }").append(Params.LINE_BREAK);
        sb.append("}").append(Params.LINE_BREAK);
        writeFile(cfg.targetPackage(), className, sb.toString());
    }
    private void writeFile(String pkg, String className, String content) {
        try {
            JavaFileObject file = filer.createSourceFile(pkg + "." + className);
            try (Writer writer = file.openWriter()) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM, yyyy - HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}
