package com.pij.noopetal;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public final class NoopetalProcessor extends AbstractProcessor {

    private static final String NOOP_CLASS_SUFFIX = "Noop";
    private static final String DECOR_CLASS_SUFFIX = "Decorating";

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = new LinkedHashSet<>();
        result.add(Noop.class.getCanonicalName());
        result.add(Decor.class.getCanonicalName());
        return result;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (env.processingOver() || env.errorRaised()) return false;

        for (TypeElement annotationElement : annotations) {
            final TypeMirror annotation = annotationElement.asType();
            if (typeUtils.isSameType(annotation, getNoopTypeMirror())) {
                processNoop(env);
            } else if (typeUtils.isSameType(annotation, getDecorTypeMirror())) {
                processDecor(env);
            }
        }

        return true;
    }

    private TypeMirror getDecorTypeMirror() {
        final TypeElement decorTypeElement = elementUtils.getTypeElement(Decor.class.getCanonicalName());
        return decorTypeElement.asType();
    }

    private TypeMirror getNoopTypeMirror() {
        final TypeElement noopTypeElement = elementUtils.getTypeElement(Noop.class.getCanonicalName());
        return noopTypeElement.asType();
    }

    private void processNoop(RoundEnvironment env) {
        Set<GeneratedClass> targetClasses = findAndParseNoopTargets(env);

        createFiles(targetClasses);
    }

    private void processDecor(RoundEnvironment env) {
        Set<GeneratedClass> targetClasses = findAndParseDecorTargets(env);

        createFiles(targetClasses);
    }

    private void createFiles(Set<GeneratedClass> sourceClasses) {
        for (GeneratedClass source : sourceClasses) {
            createFile(source);
        }
    }

    private void createFile(GeneratedClass source) {
        final JavaFile file = JavaFile.builder(source.getClassPackage(), source.getTypeSpec()).build();
        try {
            file.writeTo(filer);
        } catch (IOException e) {
            TypeElement typeElement = source.getSuperType();
            error(typeElement, "Unable to write Noop implementation of type %s: %s", typeElement, e.getMessage());
        }
    }

    private Set<GeneratedClass> findAndParseNoopTargets(RoundEnvironment env) {
        Set<GeneratedClass> targetClasses = new LinkedHashSet<>();

        // Process each @Noop element.
        for (Element element : env.getElementsAnnotatedWith(Noop.class)) {
            // Not too sure what this extra validation does, especially since it doesn't log what's wrong, but Wharton
            // has it in Butterknife.
            if (SuperficialValidation.validateElement(element) && validateNoopTarget(element)) {
                try {
                    parseNoop(element, targetClasses);
                } catch (Exception e) {
                    logParsingError(element, Noop.class, e);
                }
            }
        }

        return targetClasses;
    }

    private Set<GeneratedClass> findAndParseDecorTargets(RoundEnvironment env) {
        Set<GeneratedClass> targetClasses = new LinkedHashSet<>();

        // Process each @Decor element.
        for (Element element : env.getElementsAnnotatedWith(Decor.class)) {
            // Not too sure what this extra validation does, especially since it doesn't log what's wrong, but Wharton
            // has it in Butterknife.
            if (SuperficialValidation.validateElement(element) && validateDecorTarget(element)) {
                try {
                    parseDecor(element, targetClasses);
                } catch (Exception e) {
                    logParsingError(element, Decor.class, e);
                }
            }
        }

        return targetClasses;
    }

    /**
     * @return <code>true</code> if the element is a valid target of the {@link Noop} annotation.
     */
    private boolean validateNoopTarget(Element element) {
        return validateAnnotatedIsInterface(element, Noop.class);
    }

    /**
     * @return <code>true</code> if the element is a valid target of the {@link Decor} annotation.
     */
    private boolean validateDecorTarget(Element element) {
        return validateAnnotatedIsInterface(element, Decor.class);
    }

    /**
     * @return <code>true</code> if the element is an interface
     */
    private boolean validateAnnotatedIsInterface(Element annotated, Class<?> annotation) {
        boolean hasError = false;
        if (annotated.getKind() != INTERFACE) {
            error(annotated,
                  "@%s must only be applied to an interface. %s isn't",
                  annotation.getSimpleName(),
                  annotated.getSimpleName());
            hasError = true;
        }
        return !hasError;
    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation, Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    /**
     * This is where information about the annotation should be/is gathered.
     */
    private void parseNoop(Element element, Set<GeneratedClass> targetClasses) {
        // Nothin much to gather: there's no option/ value to gather...
        TypeElement typeElement = (TypeElement)element;

        GeneratedClass result = createNoopClass(typeElement);
        targetClasses.add(result);
    }

    /**
     * This is where information about the annotation should be/is gathered.
     */
    private void parseDecor(Element element, Set<GeneratedClass> targetClasses) {
        // Nothin much to gather: there's no option/ value to gather...
        TypeElement typeElement = (TypeElement)element;

        GeneratedClass result = createDecorClass(typeElement);
        targetClasses.add(result);
    }

    /**
     * Assumes the element is valid.
     * @param element assumed valid
     * @return a representation of the generated class
     */
    private GeneratedClass createNoopClass(TypeElement element) {
        String classPackage = getPackageName(element);
        String className = getClassName(element, classPackage) + NOOP_CLASS_SUFFIX;

        return new NoopClass(classPackage, className, element, this);
    }

    /**
     * Assumes the element is valid.
     * @param element assumed valid
     * @return a representation of the generated class
     */
    private GeneratedClass createDecorClass(TypeElement element) {
        String classPackage = getPackageName(element);
        String className = getClassName(element, classPackage) + DECOR_CLASS_SUFFIX;

        return new DecorClass(classPackage, className, element, this);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

}
