package com.olivadevelop.kore_processor;

import com.google.auto.service.AutoService;
import com.olivadevelop.kore_annotations.StaticProperties;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions("kore.version")
public class StaticPropertyGeneratorProcessor extends AbstractProcessor {
    private Filer filer;
    private Elements elements;
    private String koreVersion;
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(StaticProperties.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        elements = env.getElementUtils();
        koreVersion = env.getOptions().get("kore.version");
        if (koreVersion == null) { koreVersion = "unknown"; }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) { return false; }
        for (Element element : roundEnv.getElementsAnnotatedWith(StaticProperties.class)) {
            if (element.getKind() != ElementKind.CLASS) { continue; }
            TypeElement entity = (TypeElement) element;
            StaticProperties config = entity.getAnnotation(StaticProperties.class);
            generateProperties(entity, config);
        }
        return true;
    }
    private void generateProperties(TypeElement entity, StaticProperties config) {
        String entityName = entity.getSimpleName().toString().concat("Static");
        Set<String> imports = new HashSet<>();
        imports.add(javax.annotation.processing.Generated.class.getCanonicalName());
        imports.add(java.util.Arrays.class.getCanonicalName());
        imports.add(java.util.List.class.getCanonicalName());
        Set<String> properties = new HashSet<>();
        collectProperties(0, entity, config, properties);
        if (properties.isEmpty()) { return; }
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(Params.COM_OLIVADEVELOP_KORE).append(Params.SEMICOLON).append(Params.LINE_BREAK_DOUBLE);
        imports.stream().sorted().forEach(i -> sb.append("import ").append(i).append(Params.SEMICOLON).append(Params.LINE_BREAK));
        sb.append(Params.LINE_BREAK);
        sb.append("@Generated(value = \"").append(entity.getSimpleName()).append(".class\", date = \"").append(getCurrentDate()).append("\", comments = \"").append(getComment()).append("\")").append(Params.LINE_BREAK);
        sb.append("public final class ").append(entityName).append(" {").append(Params.LINE_BREAK);
        properties.forEach(p -> sb.append("    public static final String ").append(p).append(" = \"").append(p).append("\"").append(Params.SEMICOLON).append(Params.LINE_BREAK));
        sb.append("    public static List<String> properties() { return Arrays.asList(").append(String.join(", ", properties)).append("); }");
        sb.append("}").append(Params.LINE_BREAK);
        try {
            JavaFileObject file = filer.createSourceFile(entity.getQualifiedName().toString().replace(entity.getSimpleName().toString(), entityName));
            try (Writer writer = file.openWriter()) { writer.write(sb.toString()); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String getComment() { return "DTO Generated from version: " + koreVersion; }
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM, yyyy - HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
    private void collectProperties(int deep, TypeElement type, StaticProperties config, Set<String> properties) {
        if (deep > config.hierarchyLevel()) { return; }
        // Campos de esta clase
        for (Element field : type.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) { continue; }
            VariableElement ve = (VariableElement) field;
            if (ve.getModifiers().contains(Modifier.STATIC)) { continue; }
            String name = ve.getSimpleName().toString();
            if (Arrays.stream(config.ignore()).noneMatch(ig -> ig.equalsIgnoreCase(name))) { properties.add(name); }
        }
        // Subir al padre si procede
        if (!config.includeHierarchy()) { return; }
        if (type.getSuperclass() == null) { return; }
        Element parent = processingEnv.getTypeUtils().asElement(type.getSuperclass());
        if (!(parent instanceof TypeElement)) { return; }
        TypeElement parentType = (TypeElement) parent;
        // Cortar en Object
        if (parentType.getQualifiedName().toString().equals("java.lang.Object")) { return; }
        collectProperties(++deep, parentType, config, properties);
    }
}