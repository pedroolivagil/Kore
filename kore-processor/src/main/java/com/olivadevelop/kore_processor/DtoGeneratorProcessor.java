package com.olivadevelop.kore_processor;

import com.google.auto.service.AutoService;
import com.olivadevelop.kore_annotations.DtoField;
import com.olivadevelop.kore_annotations.GenerateDto;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
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
@SupportedSourceVersion(SourceVersion.RELEASE_11)
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
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(SEMICOLON).append(LINE_BREAK_DOUBLE);
        imports.stream().sorted().forEach(i -> sb.append("import ").append(i).append(SEMICOLON).append(LINE_BREAK));
        sb.append(LINE_BREAK);
        sb.append("@Builder").append(LINE_BREAK);
        sb.append("@Data").append(LINE_BREAK);
        sb.append("@NoArgsConstructor").append(LINE_BREAK);
        sb.append("@AllArgsConstructor").append(LINE_BREAK);
        sb.append("@EqualsAndHashCode(callSuper = true)").append(LINE_BREAK);
        sb.append("@Generated(value = \"").append(entity.getSimpleName()).append(".class\", date = \"").append(getCurrentDate()).append("\", comments = \"").append(getComment()).append("\")").append(LINE_BREAK);
        sb.append("public class ").append(dtoName).append(" extends KoreDTO {").append(LINE_BREAK);
        properties.stream().sorted(Comparator.comparing(p -> p.order)).forEach(p -> sb.append("    private ").append(p.type).append(" ").append(p.property).append(SEMICOLON).append(LINE_BREAK));
        sb.append("}").append(LINE_BREAK);
        try {
            JavaFileObject file = filer.createSourceFile(pkg + "." + dtoName);
            try (Writer writer = file.openWriter()) { writer.write(sb.toString()); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static String getComment() {
        return "DTO Generated from version 1.30";
    }
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
        imports.add(COM_OLIVADEVELOP_KORE_DB_DTO_KORE_DTO);
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
                        type = useCollection(JAVA_UTIL_LIST, dtoField.type().getCanonicalName(), imports);
                    } else if (isSet(fieldType)) {
                        type = useCollection(JAVA_UTIL_SET, dtoField.type().getCanonicalName(), imports);
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
                            type = useCollection(JAVA_UTIL_LIST, fqn, imports);
                        } else {
                            type = useCollection(JAVA_UTIL_SET, fqn, imports);
                        }
                    } else {
                        type = useCollection(JAVA_UTIL_LIST, genericElement.getQualifiedName().toString(), imports);
                    }
                } else {
                    type = useType(fieldType, imports, config.usePrimitives());
                }
            } else {
                type = useType(fieldType, imports, config.usePrimitives());
            }
            properties.add(new Property(order++, type, finalName));
        }
    }
    private static String getDtoName(TypeElement entity, GenerateDto config) {
        String entityName = nonPrimitives(entity, config.usePrimitives());
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
    private String useType(TypeMirror type, Set<String> imports, boolean nonPrimitives) {
        if (!(type instanceof DeclaredType)) { return type.toString(); }
        TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        String fqcn = element.getQualifiedName().toString();
        // No importes java.lang
        if (!fqcn.startsWith("java.lang")) { imports.add(fqcn); }
        return nonPrimitives(element, nonPrimitives);
    }
    private static String nonPrimitives(TypeElement element, boolean usePrimitives) {
        String type = element.getSimpleName().toString();
        if (!usePrimitives && typeIsPrimitive(type)) {
            switch (type) {
                case "boolean":
                    return Boolean.class.getSimpleName();
                case "byte":
                    return Byte.class.getSimpleName();
                case "char":
                    return Character.class.getSimpleName();
                case "double":
                    return Double.class.getSimpleName();
                case "float":
                    return Float.class.getSimpleName();
                case "int":
                    return Integer.class.getSimpleName();
                case "long":
                    return Long.class.getSimpleName();
                case "short":
                    return Short.class.getSimpleName();
            }
        }
        return type;
    }
    private static boolean typeIsPrimitive(String type) {
        return type.equals("boolean") || type.equals("byte") || type.equals("char") || type.equals("double")
                || type.equals("float") || type.equals("int") || type.equals("long") || type.equals("short");
    }
    private String useCollection(String collectionFqn, String genericFqn, Set<String> imports) {
        imports.add(collectionFqn);
        if (!genericFqn.startsWith("java.lang")) { imports.add(genericFqn); }
        String collectionSimple = collectionFqn.substring(collectionFqn.lastIndexOf('.') + 1);
        String genericSimple = genericFqn.substring(genericFqn.lastIndexOf('.') + 1);
        return collectionSimple + "<" + genericSimple + ">";
    }
    private static class Property {
        private final Integer order;
        private final String type;
        private final String property;
        public Property(Integer order, String type, String property) {
            this.order = order;
            this.type = type;
            this.property = property;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Property property1 = (Property) o;
            return Objects.equals(order, property1.order) && Objects.equals(type, property1.type) && Objects.equals(property, property1.property);
        }
        @Override
        public int hashCode() {
            return Objects.hash(order, type, property);
        }
    }
}
