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

import static com.pij.noopetal.GeneratedClassUtil.applyAccessModifier;
import static com.pij.noopetal.GeneratedClassUtil.createGeneratedAnnotation;
import static org.apache.commons.lang3.Validate.notNull;

final class NoopClass implements GeneratedClass {

    private final String classPackage;
    private final String className;
    private final Class<? extends Processor> processorClass;
    private final TypeElement superType;

    public NoopClass(@NonNull String classPackage, @NonNull String className, @NonNull TypeElement superType,
                     @NonNull Processor processor) {
        this.classPackage = notNull(classPackage);
        this.className = notNull(className);
        this.processorClass = notNull(processor).getClass();
        this.superType = notNull(superType);
    }

    private static MethodSpec.Builder createOverridingMethod(ExecutableElement element) {
        final MethodSpec.Builder result = MethodSpec.overriding(element);
        final String literal = GeneratedClassUtil.defaultReturnLiteral(element.getReturnType());
        if (literal != null) result.addStatement("return $L", literal);
        return result;
    }

    @Override
    public TypeElement getSuperType() {
        return superType;
    }

    @Override
    public String getClassPackage() {
        return classPackage;
    }

    /**
     * The access modifier is that of the superType.
     */
    @Override
    @NonNull
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder result = TypeSpec.classBuilder(className);
        applyAccessModifier(superType, result);
        GeneratedClassUtil.applyTypeVariables(superType, result);
        result.addJavadoc(createGeneratedAnnotation(processorClass).toString());
        result.addSuperinterface(TypeName.get(superType.asType()));

        for (Element element : superType.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                MethodSpec method = createOverridingMethod((ExecutableElement)element).build();
                result.addMethod(method);
            }
        }

        return result.build();
    }

}
