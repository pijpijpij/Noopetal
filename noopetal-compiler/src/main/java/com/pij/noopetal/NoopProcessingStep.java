package com.pij.noopetal;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Types;

import static com.pij.noopetal.ClassGenerationUtil.extractPackageAndClassName;

final class NoopProcessingStep extends ClassGenerator {

    private static final String NOOP_CLASS_PREFIX = "Noop";

    public NoopProcessingStep(@NonNull Class<? extends Processor> processorClass,
                              @NonNull ProcessingEnvironment processingEnv) {
        super(processorClass, processingEnv);
    }

    @NonNull
    protected Class<Noop> getSupportedAnnotation() {
        return Noop.class;
    }

    /**
     * @return <code>true</code> if the element is a valid target of the {@link Noop} annotation.
     */
    protected boolean validate(Element element) {
        return validateAnnotatedIsInterface(element);
    }

    /**
     * Assumes the element is valid. It uses the value specified in the annotation, if it has a package. Otherwise uses
     * the element's package.
     * @param element annotated interface, assumed valid
     * @param types   type utility, currently ignored
     * @return a representation of the generated class.
     */
    @Override
    protected GeneratedType createGeneratedClass(EnrichedTypeElement element, Class<? extends Processor> processorClass,
                                                 Types types) {

        final String specifiedClass = element.getAnnotation(getSupportedAnnotation()).value();
        final Pair<String, String> packageAndClassName = extractPackageAndClassName(specifiedClass);
        String packageName = packageAndClassName.getLeft();
        if (packageName == null) {
            final PackageElement elementPackage = element.getPackage();
            packageName = elementPackage.getQualifiedName().toString();
        }
        String className = packageAndClassName.getRight();
        if (className == null) {
            className = element.calculateClassNameWithPrefix(getClassPrefix());
        }
        return new NoopClass(packageName, className, element, processorClass, types);
    }

    @NonNull
    protected String getClassPrefix() {
        return NOOP_CLASS_PREFIX;
    }

}
