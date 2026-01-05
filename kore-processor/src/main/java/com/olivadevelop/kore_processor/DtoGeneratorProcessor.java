package com.olivadevelop.kore_processor;

import com.google.auto.service.AutoService;
import com.olivadevelop.kore_annotations.DtoField;
import com.olivadevelop.kore_annotations.GenerateDto;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Comparator;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions("kore.version")
public class DtoGeneratorProcessor extends AbstractProcessor {
    private Filer filer;
    private Elements elements;
    private String koreVersion;
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(GenerateDto.class.getCanonicalName());
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
        Set<Property> properties = new HashSet<>();
        calculateClass(entity, config, imports, properties);
        imports.add(entity.getQualifiedName().toString());
        if (properties.isEmpty()) { imports.remove(lombok.AllArgsConstructor.class.getCanonicalName()); }
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(Params.SEMICOLON).append(Params.LINE_BREAK_DOUBLE);
        imports.stream().sorted().forEach(i -> sb.append("import ").append(i).append(Params.SEMICOLON).append(Params.LINE_BREAK));
        sb.append(Params.LINE_BREAK);
        sb.append("@Builder").append(Params.LINE_BREAK);
        sb.append("@Data").append(Params.LINE_BREAK);
        sb.append("@NoArgsConstructor").append(Params.LINE_BREAK);
        if (!properties.isEmpty()) { sb.append("@AllArgsConstructor").append(Params.LINE_BREAK); }
        sb.append("@EqualsAndHashCode(callSuper = true)").append(Params.LINE_BREAK);
        sb.append("@Generated(value = \"")
                .append(entity.getSimpleName())
                .append(".class\", date = \"")
                .append(getCurrentDate())
                .append("\", comments = \"")
                .append(getComment()).append("\")").append(Params.LINE_BREAK);
        sb.append("public class ").append(dtoName).append(" extends KoreDTO<")
                .append(entity.getSimpleName().toString())
                .append("> {")
                .append(Params.LINE_BREAK);
        properties.stream()
                .sorted(Comparator.comparing(Property::getOrder))
                .forEach(p -> sb.append("    private ").append(p.getType()).append(" ").append(p.getProperty()).append(Params.SEMICOLON).append(Params.LINE_BREAK));
        sb.append("}").append(Params.LINE_BREAK);
        try {
            JavaFileObject file = filer.createSourceFile(pkg + "." + dtoName);
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
    private void calculateClass(TypeElement entity, GenerateDto config, Set<String> imports, Set<Property> properties) {
        imports.add(lombok.Builder.class.getCanonicalName());
        imports.add(lombok.Data.class.getCanonicalName());
        imports.add(lombok.NoArgsConstructor.class.getCanonicalName());
        imports.add(lombok.AllArgsConstructor.class.getCanonicalName());
        imports.add(lombok.EqualsAndHashCode.class.getCanonicalName());
        imports.add(javax.annotation.processing.Generated.class.getCanonicalName());
        imports.add(Params.COM_OLIVADEVELOP_KORE_DB_DTO_KORE_DTO);
        int order = 0;
        for (Element field : entity.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) { continue; }
            VariableElement ve = (VariableElement) field;
            // Ignorar static
            Set<Modifier> modifiers = ve.getModifiers();
            if (modifiers.contains(Modifier.STATIC)) { continue; }
            String propertyPrefix = config.propertyPrefix();
            DtoField dtoField = ve.getAnnotation(DtoField.class);
            TypeMirror fieldType = ve.asType();
            String type;
            String originalName = ve.getSimpleName().toString();
            String finalName = propertyPrefix + originalName;
            if (dtoField != null) {
                if (dtoField.ignore()) { continue; }
                if (!dtoField.name().isEmpty()) { finalName = dtoField.name(); }
                if (dtoField.type() != null && dtoField.type() != void.class) {
                    if (isList(fieldType)) {
                        type = useCollection(Params.JAVA_UTIL_LIST, dtoField.type().getCanonicalName(), imports);
                    } else if (isSet(fieldType)) {
                        type = useCollection(Params.JAVA_UTIL_SET, dtoField.type().getCanonicalName(), imports);
                    } else {
                        type = dtoField.type().getSimpleName();
                        imports.add(dtoField.type().getCanonicalName());
                    }
                } else {
                    type = useType(fieldType, imports, config.usePrimitives());
                }
            } else if (isList(fieldType) || isSet(fieldType)) {
                DeclaredType declaredType = (DeclaredType) fieldType;
                if (!declaredType.getTypeArguments().isEmpty()) {
                    TypeMirror genericType = declaredType.getTypeArguments().get(0);
                    TypeElement genericElement = (TypeElement) processingEnv.getTypeUtils().asElement(genericType);
                    GenerateDto genDto = genericElement.getAnnotation(GenerateDto.class);
                    if (genDto != null) {
                        String name = getDtoName(genericElement, genDto);
                        String fqn = genDto.dtoPackage().concat(".").concat(name);
                        if (isList(fieldType)) {
                            type = useCollection(Params.JAVA_UTIL_LIST, fqn, imports);
                        } else {
                            type = useCollection(Params.JAVA_UTIL_SET, fqn, imports);
                        }
                    } else {
                        type = useCollection(Params.JAVA_UTIL_LIST, genericElement.getQualifiedName().toString(), imports);
                    }
                } else {
                    type = useType(fieldType, imports, config.usePrimitives());
                }
            } else {
                // No es una lista ni un set, ni contine la anotacion dtoField
                type = useType(fieldType, imports, config.usePrimitives());
            }
            properties.add(new Property(order++, type, finalName));
        }
    }
    private static String getDtoName(TypeElement entity, GenerateDto config) {
        String entityName = entity.getSimpleName().toString();
        return config.name().isEmpty() ? entityName + config.suffix() : config.name();
    }
    private boolean isList(TypeMirror type) {
        if (!(type instanceof DeclaredType)) { return false; }
        String raw = ((DeclaredType) type).asElement().toString();
        return raw.equals(Params.JAVA_UTIL_LIST);
    }
    private boolean isSet(TypeMirror type) {
        if (!(type instanceof DeclaredType)) { return false; }
        String raw = ((DeclaredType) type).asElement().toString();
        return raw.equals(Params.JAVA_UTIL_SET);
    }
    private String useType(TypeMirror type, Set<String> imports, boolean usePrimitives) {
        if (type.getKind().isPrimitive()) { // int, boolean, etc.
            if (usePrimitives) { return type.toString(); }
            switch (type.getKind()) {
                case BOOLEAN:
                    return Boolean.class.getSimpleName();
                case BYTE:
                    return Byte.class.getSimpleName();
                case CHAR:
                    return Character.class.getSimpleName();
                case DOUBLE:
                    return Double.class.getSimpleName();
                case FLOAT:
                    return Float.class.getSimpleName();
                case INT:
                    return Integer.class.getSimpleName();
                case LONG:
                    return Long.class.getSimpleName();
                case SHORT:
                    return Short.class.getSimpleName();
                default:
                    return type.toString();
            }
        }
        if (!(type instanceof DeclaredType)) { return type.toString(); }
        TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        GenerateDto config = element.getAnnotation(GenerateDto.class);
        if (config == null) {
            String fqcn = element.getQualifiedName().toString();
            // No importes java.lang
            if (!fqcn.startsWith("java.lang")) { imports.add(fqcn); }
            return element.getSimpleName().toString();
        } else {
            String name = getDtoName(element, config);
            String fqn = config.dtoPackage().concat(".").concat(name);
            imports.add(fqn);
            return name;
        }
    }
    private String useCollection(String collectionFqn, String genericFqn, Set<String> imports) {
        imports.add(collectionFqn);
        if (!genericFqn.startsWith("java.lang")) { imports.add(genericFqn); }
        String collectionSimple = collectionFqn.substring(collectionFqn.lastIndexOf('.') + 1);
        String genericSimple = genericFqn.substring(genericFqn.lastIndexOf('.') + 1);
        return collectionSimple + "<" + genericSimple + ">";
    }
}
