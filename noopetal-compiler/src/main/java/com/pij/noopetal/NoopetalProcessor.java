package com.pij.noopetal;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.pij.noopetal.GeneratedClassUtil.extractPackageAndClassName;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public final class NoopetalProcessor extends AbstractProcessor {

    private static final String NOOP_CLASS_PREFIX = "Noop";
    private static final String DECOR_CLASS_PREFIX = "Decorating";

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
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (env.processingOver() || env.errorRaised()) return false;

        for (TypeElement annotation : annotations) {
            if (isSame(annotation, Noop.class)) {
                processNoop(env);
            } else if (isSame(annotation, Decor.class)) {
                processDecor(env);
            }
        }

        return true;
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
        final JavaFile file = JavaFile.builder(source.getClassPackage(), source.getTypeSpec())
                                      .skipJavaLangImports(true)
                                      .build();
        try {
            file.writeTo(getFiler());
        } catch (IOException e) {
            final TypeElement sourceType = source.getSourceType();
            final String className = source.getClassName();
            error(sourceType, "Unable to write file %s of type %s: %s", className, sourceType, e.getMessage());
        }
    }

    private Set<GeneratedClass> findAndParseNoopTargets(RoundEnvironment env) {
        Set<GeneratedClass> result = new LinkedHashSet<>();

        // Process each @Noop element.
        for (Element element : env.getElementsAnnotatedWith(Noop.class)) {
            // Not too sure what this extra validation does, especially since it doesn't log what's wrong, but Wharton
            // has it in Butterknife.
            if (SuperficialValidation.validateElement(element) && validateNoopTarget(element)) {
                try {
                    parseNoop(element, result);
                } catch (Exception e) {
                    logParsingError(element, Noop.class, e);
                }
            }
        }

        return result;
    }

    private Set<GeneratedClass> findAndParseDecorTargets(RoundEnvironment env) {
        Set<GeneratedClass> result = new LinkedHashSet<>();

        // Process each @Decor element.
        for (Element element : env.getElementsAnnotatedWith(Decor.class)) {
            // Not too sure what this extra validation does, especially since it doesn't log what's wrong, but Wharton
            // has it in Butterknife.
            if (SuperficialValidation.validateElement(element) && validateDecorTarget(element)) {
                try {
                    parseDecor(element, result);
                } catch (Exception e) {
                    logParsingError(element, Decor.class, e);
                }
            }
        }

        return result;
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
        error(element, "Unable to parse @%s annotation.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    /**
     * This is where information about the annotation should be/is gathered.
     */
    private void parseNoop(Element element, Set<GeneratedClass> targetClasses) {
        // Nothing much to gather: there's no option/ value to gather...
        TypeElement typeElement = (TypeElement)element;

        GeneratedClass result = createNoopClass(typeElement);
        targetClasses.add(result);
    }

    /**
     * This is where information about the annotation should be/is gathered.
     */
    private void parseDecor(Element element, Set<GeneratedClass> targetClasses) {
        // Nothing much to gather: there's no option/ value to gather...
        TypeElement typeElement = (TypeElement)element;

        GeneratedClass result = createDecorClass(typeElement);
        targetClasses.add(result);
    }

    /**
     * Assumes the element is valid. It uses the value specified in the annotation, if it has a package. Otherwise uses
     * the element's package.
     * @param element annotated interface, assumed valid
     * @return a representation of the generated class.
     */
    private GeneratedClass createNoopClass(TypeElement element) {
        String specifiedClass = element.getAnnotation(Noop.class).value();
        Pair<String, String> packageAndClassName = extractPackageAndClassName(specifiedClass);
        final PackageElement elementPackage = getElementUtils().getPackageOf(element);
        String packageName = packageAndClassName.getLeft();
        if (packageName == null) {
            packageName = elementPackage.getQualifiedName().toString();
        }
        String className = packageAndClassName.getRight();
        if (className == null) {
            className = GeneratedClassUtil.calculateGeneratedClassName(element, elementPackage, NOOP_CLASS_PREFIX);
        }
        return new NoopClass(packageName, className, element, this);
    }

    /**
     * Assumes the element is valid.
     * @param element assumed valid
     * @return a representation of the generated class
     */

    private GeneratedClass createDecorClass(TypeElement element) {
        final PackageElement elementPackage = getElementUtils().getPackageOf(element);
        String className = GeneratedClassUtil.calculateGeneratedClassName(element, elementPackage, DECOR_CLASS_PREFIX);

        return new DecorClass(elementPackage.getQualifiedName().toString(), className, element, this);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        getMessager().printMessage(ERROR, message, element);
    }

    private boolean isSame(TypeElement element, Class<?> classe) {
        return getTypeUtils().isSameType(element.asType(),
                                         getElementUtils().getTypeElement(classe.getCanonicalName()).asType());
    }

}
