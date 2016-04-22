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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;

import static com.pij.noopetal.ClassGenerationUtil.createGeneratedAnnotation;
import static org.apache.commons.lang3.Validate.notNull;

final class NoopClass implements GeneratedType {

    private final String classPackage;
    private final String className;
    private final Class<? extends Processor> processorClass;
    private final EnrichedTypeElement sourceType;
    private final Types types;

    public NoopClass(@NonNull String classPackage, @NonNull String className, @NonNull EnrichedTypeElement sourceType,
                     @NonNull Class<? extends Processor> processorClass, @NonNull Types types) {
        this.classPackage = notNull(classPackage);
        this.className = notNull(className);
        this.processorClass = notNull(processorClass);
        this.types = notNull(types);
        this.sourceType = notNull(sourceType);
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
        TypeSpec.Builder result = TypeSpec.classBuilder(getTypeName());
        sourceType.applyAccessModifier(result);
        sourceType.applyTypeVariables(result);
        result.addJavadoc(createGeneratedAnnotation(processorClass).toString());
        result.addJavadoc("\n");
        result.addSuperinterface(getDecoratedTypeName());

        for (Element element : sourceType.getAllEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                MethodSpec method = createOverridingMethod((ExecutableElement)element).build();
                result.addMethod(method);
            }
        }

        return result.build();
    }

    private TypeName getDecoratedTypeName() {
        return TypeName.get(sourceType.asType());
    }

    private MethodSpec.Builder createOverridingMethod(ExecutableElement element) {
        // TODO fix the cast
        MethodSpec.Builder result = MethodSpec.overriding(element, (DeclaredType)getSourceType().asType(), types);
        String literal = ClassGenerationUtil.defaultReturnLiteral(element.getReturnType());
        if (literal != null) result.addStatement("return $L", literal);
        return result;
    }

}
