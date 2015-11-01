package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import static com.pij.noopetal.ClassGenerationUtil.createGeneratedAnnotation;
import static org.apache.commons.lang3.Validate.notNull;

final class NoopClass implements GeneratedClass {

    private final String classPackage;
    private final String className;
    private final Class<? extends Processor> processorClass;
    private final EnrichedTypeElement sourceType;

    public NoopClass(@NonNull String classPackage, @NonNull String className, @NonNull EnrichedTypeElement sourceType,
                     @NonNull Processor processor) {
        this.classPackage = notNull(classPackage);
        this.className = notNull(className);
        this.processorClass = notNull(processor).getClass();
        this.sourceType = notNull(sourceType);
    }

    private static MethodSpec.Builder createOverridingMethod(ExecutableElement element) {
        final MethodSpec.Builder result = MethodSpec.overriding(element);
        final String literal = ClassGenerationUtil.defaultReturnLiteral(element.getReturnType());
        if (literal != null) result.addStatement("return $L", literal);
        return result;
    }

    @Override
    public TypeElement getSourceType() {
        return sourceType.getTypeElement();
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getClassPackage() {
        return classPackage;
    }

    /**
     * The access modifier is that of the sourceType.
     */
    @Override
    @NonNull
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder result = TypeSpec.classBuilder(className);
        sourceType.applyAccessModifier(result);
        sourceType.applyTypeVariables(result);
        result.addJavadoc(createGeneratedAnnotation(processorClass).toString());
        result.addSuperinterface(TypeName.get(sourceType.asType()));

        for (Element element : sourceType.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                MethodSpec method = createOverridingMethod((ExecutableElement)element).build();
                result.addMethod(method);
            }
        }

        return result.build();
    }

}
