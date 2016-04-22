package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.SuperficialValidation;
import com.google.common.collect.SetMultimap;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.tools.Diagnostic.Kind.ERROR;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Contains common code for processing an annotation that results in the generation of a single Java file.
 */
abstract class ClassGenerator implements BasicAnnotationProcessor.ProcessingStep {

    private final Class<? extends Processor> processorClass;
    private final ProcessingEnvironment processingEnv;

    public ClassGenerator(@NonNull Class<? extends Processor> processorClass,
                          @NonNull ProcessingEnvironment processingEnv) {
        this.processorClass = notNull(processorClass);
        this.processingEnv = notNull(processingEnv);
    }

    @Override
    public final Set<? extends Class<? extends Annotation>> annotations() {
        return Collections.singleton(getSupportedAnnotation());
    }

    @Override
    public final Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {

        Set<GeneratedType> targetClasses = findAndParseTargets(elementsByAnnotation.get(getSupportedAnnotation()));
        createFiles(targetClasses);
        // TODO Look into this: did we rally process all elements?
        return Collections.emptySet();
    }

    private Elements getElementUtils() {
        return processingEnv.getElementUtils();
    }

    private Types getTypeUtils() {
        return processingEnv.getTypeUtils();
    }

    private Filer getFiler() {
        return processingEnv.getFiler();
    }

    private Messager getMessager() {
        return processingEnv.getMessager();
    }

    private void createFiles(Set<GeneratedType> sourceClasses) {
        for (GeneratedType source : sourceClasses) {
            createFile(source);
        }
    }

    private void createFile(GeneratedType source) {
        final JavaFile file = JavaFile.builder(source.getTypePackage(), source.getTypeSpec())
                                      .skipJavaLangImports(true)
                                      .build();
        try {
            file.writeTo(getFiler());
        } catch (IOException e) {
            final TypeElement sourceType = source.getSourceType();
            final String className = source.getTypeName();
            error(sourceType, "Unable to write file %s of type %s: %s", className, sourceType, e.getMessage());
        }
    }

    // Process each annotated element.
    private Set<GeneratedType> findAndParseTargets(Set<Element> elements) {
        Set<GeneratedType> result = new LinkedHashSet<>();

        for (Element element : elements) {
            // Not too sure what this extra validation does, especially since it doesn't log what's wrong, but Wharton
            // has it in Butterknife.
            if (SuperficialValidation.validateElement(element) && validate(element)) {
                try {
                    parse(element, result);
                } catch (Exception e) {
                    logParsingError(element, getSupportedAnnotation(), e);
                }
            }
        }

        return result;
    }

    @NonNull
    protected abstract Class<? extends Annotation> getSupportedAnnotation();

    /**
     * @return <code>true</code> if the element is a valid target of the {@link Noop} annotation.
     */
    protected abstract boolean validate(Element element);

    /**
     * @return <code>true</code> if the element is an interface
     */
    protected final boolean validateAnnotatedIsInterface(Element annotated) {
        boolean hasError = false;
        if (annotated.getKind() != INTERFACE) {
            error(annotated,
                  "@%s must only be applied to an interface. %s isn't",
                  getSupportedAnnotation().getSimpleName(),
                  annotated.getSimpleName());
            hasError = true;
        }
        return !hasError;
    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation, Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s annotation.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    /**
     * This is where information about the annotation should be/is gathered.
     */
    private void parse(Element element, Set<GeneratedType> targetClasses) {
        TypeElement typeElement = (TypeElement)element;

        GeneratedType result = createGeneratedClass(new EnrichedTypeElement(typeElement, getElementUtils()),
                                                    processorClass,
                                                    getTypeUtils());
        targetClasses.add(result);
    }

    /**
     * Assumes the element is valid. It uses the value specified in the annotation, if it has a package. Otherwise uses
     * the element's package.
     * @param element annotated interface, assumed valid
     * @param types   type utility provided by the processor environment
     * @return a representation of the generated class.
     */
    protected abstract GeneratedType createGeneratedClass(EnrichedTypeElement element,
                                                          Class<? extends Processor> processorClass, Types types);

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        getMessager().printMessage(ERROR, message, element);
    }

}
