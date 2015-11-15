package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.pij.noopetal.ClassGenerationUtil.createGeneratedAnnotation;
import static org.apache.commons.lang3.Validate.notNull;

final class FactoryInterface implements GeneratedType {

    private final String classPackage;
    private final String className;
    private final Class<? extends Processor> processorClass;
    private final EnrichedTypeElement sourceType;

    public FactoryInterface(@NonNull String classPackage, @NonNull String className,
                            @NonNull EnrichedTypeElement sourceType,
                            @NonNull Class<? extends Processor> processorClass) {
        this.classPackage = notNull(classPackage);
        this.className = notNull(className);
        this.processorClass = notNull(processorClass);
        this.sourceType = notNull(sourceType);
    }

    private MethodSpec.Builder createCreateMethod() {
        final MethodSpec.Builder result = MethodSpec.methodBuilder("create" + getSourceType().getSimpleName());
        result.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
        result.returns(TypeName.get(getSourceType().asType()));
        return result;
    }

    @Override
    public TypeElement getSourceType() {
        return sourceType.getTypeElement();
    }

    @Override
    public String getTypeName() {
        return className;
    }

    @Override
    public String getTypePackage() {
        return classPackage;
    }

    /**
     * The access modifier is that of the sourceType.
     */
    @NonNull
    @Override
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder result = TypeSpec.interfaceBuilder(getTypeName());
        sourceType.applyAccessModifier(result);
        sourceType.applyTypeVariables(result);
        result.addJavadoc(createGeneratedAnnotation(processorClass).toString());
        result.addJavadoc("\n");

        MethodSpec method = createCreateMethod().build();
        result.addMethod(method);

        return result.build();
    }

}
