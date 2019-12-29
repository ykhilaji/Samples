package com.nryanov;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.nryanov.HelloWorld")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public final class CustomProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> elements = new ArrayList<>();

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() != ElementKind.INTERFACE) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Only interfaces may be annotated with HelloWorld");
                    return true;
                }

                TypeElement typeElement = (TypeElement) element;
                elements.add(typeElement);
            }
        }

        return generate(filer, elements);
    }

    public boolean generate(Filer filer, List<TypeElement> elements) {
        try {
            for (TypeElement element : elements) {
                String className = element.getSimpleName().toString() + "Impl";
                String fullClassName = element.getQualifiedName().toString() + "Impl";

                MethodSpec main = MethodSpec.methodBuilder("main")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(String[].class, "args")
                        .addStatement("$T.out.println($S)", System.class, String.format("Hello world from %s", element.getSimpleName().toString()))
                        .build();

                TypeSpec helloWorld = TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(main)
                        .build();

                JavaFile javaFile = JavaFile.builder("sample", helloWorld)
                        .build();

                javaFile.writeTo(filer);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
            return true;
        }

        return false;
    }
}
