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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public final class NoopetalProcessor extends AbstractProcessor {

    private static final String NOOP_CLASS_SUFFIX = "Noop";

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
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
        Set<NoopClass> targetClasses = findAndParseTargets(env);

        for (NoopClass target : targetClasses) {
            final JavaFile file = JavaFile.builder(target.getClassPackage(), target.getTypeSpec()).build();
            try {
                file.writeTo(filer);
            } catch (IOException e) {
                TypeElement typeElement = target.getSuperType();
                error(typeElement, "Unable to write Noop implementation of type %s: %s", typeElement, e.getMessage());
            }
        }

        return true;
    }

    private Set<NoopClass> findAndParseTargets(RoundEnvironment env) {
        Set<NoopClass> targetClasses = new LinkedHashSet<>();

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

    /**
     * @return <code>true</code> if the element is a valid target thee {@link Noop} annotation.
     */
    private boolean validateNoopTarget(Element element) {
        boolean hasError = false;
        if (element.getKind() != INTERFACE) {
            error(element, "@%s must only be applied to an interface. %s isn't", Noop.class.getSimpleName(),
                  element.getSimpleName());
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
    private void parseNoop(Element element, Set<NoopClass> targetClasses) {
        // Nothin much to gather: there's no option/ value to gather...
        TypeElement typeElement = (TypeElement)element;

        NoopClass result = createNoopClass(typeElement);
        targetClasses.add(result);
    }

    /**
     * Assumes the element is valid.
     * @param element assumed valid
     * @return a representation of the generated class
     */
    private NoopClass createNoopClass(TypeElement element) {
        String classPackage = getPackageName(element);
        String className = getClassName(element, classPackage) + NOOP_CLASS_SUFFIX;

        return new NoopClass(classPackage, className, element, this);
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
