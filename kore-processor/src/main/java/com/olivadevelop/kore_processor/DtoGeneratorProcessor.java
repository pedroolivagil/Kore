package com.olivadevelop.kore_processor;

import com.google.auto.service.AutoService;
import com.olivadevelop.kore_annotations.GenerateDto;

import java.io.IOException;
import java.io.Writer;
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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DtoGeneratorProcessor extends AbstractProcessor {

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
//        processingEnv.getMessager().printMessage(
//                Diagnostic.Kind.ERROR,
//                "🔥 GenerateDtoProcessor INICIALIZADO"
//        );
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
        String entityName = entity.getSimpleName().toString();
        String dtoName = entityName + config.suffix();
        String pkg = config.dtoPackage();
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import lombok.*;\n\n");
        if (config.data()) { sb.append("@Data\n"); }
        if (config.builder()) { sb.append("@Builder\n"); }
        sb.append("@NoArgsConstructor\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("public class ").append(dtoName).append(" {\n\n");
        for (Element field : entity.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) { continue; }
            VariableElement ve = (VariableElement) field;
            // Ignorar static / transient
            Set<Modifier> modifiers = ve.getModifiers();
            if (modifiers.contains(Modifier.STATIC)) { continue; }
            String type = ve.asType().toString();
            String name = ve.getSimpleName().toString();
            sb.append("    private ").append(type)
                    .append(" ").append(name).append(";\n");
        }
        sb.append("}\n");
        try {
            JavaFileObject file = filer.createSourceFile(pkg + "." + dtoName);
            try (Writer writer = file.openWriter()) {
                writer.write(sb.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
