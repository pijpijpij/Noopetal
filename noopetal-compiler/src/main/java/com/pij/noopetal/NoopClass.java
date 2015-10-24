package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static org.apache.commons.lang3.Validate.notNull;

final class NoopClass {

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

    public TypeElement getSuperType() {
        return superType;
    }

    public String getClassPackage() {
        return classPackage;
    }

    @NonNull
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder result = TypeSpec.classBuilder(className);
        addAccessModifiers(result);
        result.addJavadoc(createGeneratedAnnotation().toString());
        result.addSuperinterface(TypeName.get(superType.asType()));

        for (Element element : superType.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                MethodSpec method = createOverridingMethod((ExecutableElement)element).build();
                result.addMethod(method);
            }
        }

        return result.build();
    }

    private void addAccessModifiers(TypeSpec.Builder result) {
        final Modifier modifier = getAccessModifier();
        if (modifier != null) result.addModifiers(modifier);
    }

    private MethodSpec.Builder createOverridingMethod(ExecutableElement element) {
        final MethodSpec.Builder result = MethodSpec.overriding(element);
        final String literal = calculateDefaultLiteral(element.getReturnType());
        if (literal != null) result.addStatement("return $L", literal);
        return result;
    }

    private String calculateDefaultLiteral(TypeMirror returnType) {
        switch (returnType.getKind()) {
            case VOID:
                return null;
            case BOOLEAN:
                return "false";
            case BYTE:
            case CHAR:
            case INT:
                return "0";
            case LONG:
                return "0L";
            case FLOAT:
                return "0f";
            case DOUBLE:
                return "0d";
            default:
                return "null";
        }
    }

    @NonNull
    private AnnotationSpec createGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                             .addMember("value",
                                        "\"$N\"",
                                        TypeSpec.classBuilder(processorClass.getCanonicalName()).build())
                             .build();
    }

    /**
     * @return the access modifier of the supertype.
     */
    private Modifier getAccessModifier() {
        final Set<Modifier> modifiers = superType.getModifiers();
        for (Modifier modifier : modifiers) {
            switch (modifier) {
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                    return modifier;
            }
        }
        return null;
    }
}
