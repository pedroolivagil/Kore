package com.olivadevelop.kore_processor;

import com.google.auto.service.AutoService;
import com.olivadevelop.kore_annotations.DtoField;
import com.olivadevelop.kore_annotations.GenerateDto;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DtoGeneratorProcessor extends AbstractProcessor {

    public static final String COM_OLIVADEVELOP_KORE_DB_DTO_KORE_DTO = "com.olivadevelop.kore.db.dto.KoreDTO";
    public static final String JAVA_UTIL_LIST = "java.util.List";
    public static final String JAVA_UTIL_SET = "java.util.Set";
    public static final String SEMICOLON = ";";
    public static final String LINE_BREAK_DOUBLE = "\n\n";
    public static final String LINE_BREAK = "\n";
    private Filer filer;
    private Elements elements;
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(GenerateDto.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        elements = env.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateDto.class)) {
            if (element.getKind() != ElementKind.CLASS) { continue; }
            TypeElement entity = (TypeElement) element;
            GenerateDto config = entity.getAnnotation(GenerateDto.class);
            generateDto(entity, config);
        }
        return true;
    }
    private void generateDto(TypeElement entity, GenerateDto config) {
        String dtoName = getDtoName(entity, config);
        String pkg = config.dtoPackage();
        Set<String> imports = new HashSet<>();
        Map<String, String> properties = new HashMap<>();
        calculateClass(entity, config, imports, properties);
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(SEMICOLON).append(LINE_BREAK_DOUBLE);
        imports.forEach(i -> sb.append("import ").append(i).append(SEMICOLON).append(LINE_BREAK));
        sb.append("public class ").append(dtoName).append(" extends KoreDTO {").append(LINE_BREAK_DOUBLE);
        properties.forEach((t, n) -> sb.append("    private ").append(t).append(" ").append(n).append(SEMICOLON).append(LINE_BREAK));
        sb.append("}").append(LINE_BREAK);
        try {
            JavaFileObject file = filer.createSourceFile(pkg + "." + dtoName);
            try (Writer writer = file.openWriter()) { writer.write(sb.toString()); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void calculateClass(TypeElement entity, GenerateDto config, Set<String> imports, Map<String, String> properties) {
        imports.add(lombok.Builder.class.getCanonicalName());
        imports.add(lombok.Data.class.getCanonicalName());
        imports.add(lombok.NoArgsConstructor.class.getCanonicalName());
        imports.add(lombok.AllArgsConstructor.class.getCanonicalName());
        imports.add(COM_OLIVADEVELOP_KORE_DB_DTO_KORE_DTO);
        for (Element field : entity.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) { continue; }
            VariableElement ve = (VariableElement) field;
            // Ignorar static
            Set<Modifier> modifiers = ve.getModifiers();
            if (modifiers.contains(Modifier.STATIC)) { continue; }
            String propertyPrefix = config.propertyPrefix();
            DtoField dtoField = ve.getAnnotation(DtoField.class);
            TypeMirror fieldType = ve.asType();
            String type = useType(fieldType, imports);
            String originalName = ve.getSimpleName().toString();
            String finalName = propertyPrefix + originalName;
            if (dtoField != null) {
                if (dtoField.ignore()) { continue; }
                if (!dtoField.name().isEmpty()) { finalName = dtoField.name(); }
                if (dtoField.type() != null && dtoField.type() != void.class) {
                    if (isList(fieldType)) {
                        type = useCollection(JAVA_UTIL_LIST, dtoField.type().getCanonicalName(), imports);
                    } else if (isSet(fieldType)) {
                        type = useCollection(JAVA_UTIL_SET, dtoField.type().getCanonicalName(), imports);
                    } else {
                        type = dtoField.type().getSimpleName();
                        imports.add(dtoField.type().getCanonicalName());
                    }
                }
            } else if (isList(fieldType) || isSet(fieldType)) {
                DeclaredType declaredType = (DeclaredType) fieldType;
                if (!declaredType.getTypeArguments().isEmpty()) {
                    TypeMirror genericType = declaredType.getTypeArguments().get(0);
                    TypeElement genericElement = (TypeElement) processingEnv.getTypeUtils().asElement(genericType);
                    GenerateDto genDto = genericElement.getAnnotation(GenerateDto.class);
                    if (genDto == null) { continue; }
                    String name = getDtoName(genericElement, genDto);
                    if (isList(fieldType)) {
                        type = useCollection(JAVA_UTIL_LIST, name, imports);
                    } else if (isSet(fieldType)) {
                        type = useCollection(JAVA_UTIL_SET, name, imports);
                    }
                }
            }
            properties.put(type, finalName);
        }
    }
    private void generateDto2(TypeElement entity, GenerateDto config) {
        Set<String> imports = new HashSet<>();
        imports.add("lombok.*");
        imports.add(COM_OLIVADEVELOP_KORE_DB_DTO_KORE_DTO);
        String dtoName = getDtoName(entity, config);
        String pkg = config.dtoPackage();
        String propertyPrefix = config.propertyPrefix();
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import lombok.*;\n\n");
        sb.append("import com.olivadevelop.kore.db.dto.KoreDTO;\n\n");
        if (config.data()) { sb.append("@Data\n"); }
        if (config.builder()) { sb.append("@Builder\n"); }
        sb.append("@NoArgsConstructor\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("public class ").append(dtoName).append(" extends KoreDTO {\n\n");
        for (Element field : entity.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) { continue; }
            VariableElement ve = (VariableElement) field;
            // Ignorar static / transient
            Set<Modifier> modifiers = ve.getModifiers();
            if (modifiers.contains(Modifier.STATIC)) { continue; }
            DtoField dtoField = ve.getAnnotation(DtoField.class);
            TypeMirror fieldType = ve.asType();
            String type = useType(fieldType, imports);
            String originalName = ve.getSimpleName().toString();
            String finalName = propertyPrefix + originalName;
            if (dtoField != null) {
                if (dtoField.ignore()) { continue; }
                if (!dtoField.name().isEmpty()) { finalName = dtoField.name(); }
                if (dtoField.type() != null && dtoField.type() != void.class) {
                    if (isList(fieldType)) {
//                        type = "java.util.List<" + dtoField.type().getCanonicalName() + ">";
                        type = useCollection(JAVA_UTIL_LIST, dtoField.type().getCanonicalName(), imports);
                    } else if (isSet(fieldType)) {
//                        type = "java.util.Set<" + dtoField.type().getCanonicalName() + ">";
                        type = useCollection(JAVA_UTIL_SET, dtoField.type().getCanonicalName(), imports);
                    } else {
                        type = dtoField.type().getSimpleName();
                        imports.add(dtoField.type().getCanonicalName());
                    }
                }
            } else if (isList(fieldType) || isSet(fieldType)) {
                DeclaredType declaredType = (DeclaredType) fieldType;
                if (!declaredType.getTypeArguments().isEmpty()) {
                    TypeMirror genericType = declaredType.getTypeArguments().get(0);
                    TypeElement genericElement = (TypeElement) processingEnv.getTypeUtils().asElement(genericType);
                    GenerateDto genDto = genericElement.getAnnotation(GenerateDto.class);
                    if (genDto == null) { continue; }
                    String name = getDtoName(genericElement, genDto);
                    if (isList(fieldType)) {
//                        type = "java.util.List<" + name + ">";
                        type = useCollection(JAVA_UTIL_LIST, name, imports);
                    } else if (isSet(fieldType)) {
//                        type = "java.util.Set<" + name + ">";
                        type = useCollection(JAVA_UTIL_SET, name, imports);
                    }
                }
            }
            sb.append("    private ").append(type).append(" ").append(finalName).append(";\n");
        }
        sb.append("}\n");
        try {
            JavaFileObject file = filer.createSourceFile(pkg + "." + dtoName);
            try (Writer writer = file.openWriter()) { writer.write(sb.toString()); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static String getDtoName(TypeElement entity, GenerateDto config) {
        String entityName = entity.getSimpleName().toString();
        return config.name().isEmpty() ? entityName + config.suffix() : config.name();
    }
    private boolean isList(TypeMirror type) {
        if (!(type instanceof DeclaredType)) { return false; }
        String raw = ((DeclaredType) type).asElement().toString();
        return raw.equals(JAVA_UTIL_LIST);
    }
    private boolean isSet(TypeMirror type) {
        if (!(type instanceof DeclaredType)) { return false; }
        String raw = ((DeclaredType) type).asElement().toString();
        return raw.equals(JAVA_UTIL_SET);
    }
    private String useType(TypeMirror type, Set<String> imports) {
        if (!(type instanceof DeclaredType)) { return type.toString(); }
        TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        String fqcn = element.getQualifiedName().toString();
        // No importes java.lang
        if (!fqcn.startsWith("java.lang")) { imports.add(fqcn); }
        return element.getSimpleName().toString();
    }
    private String useCollection(String collectionFqn, String genericFqn, Set<String> imports) {
        imports.add(collectionFqn);
        imports.add(genericFqn);
        String collectionSimple = collectionFqn.substring(collectionFqn.lastIndexOf('.') + 1);
        String genericSimple = genericFqn.substring(genericFqn.lastIndexOf('.') + 1);
        return collectionSimple + "<" + genericSimple + ">";
    }

}
