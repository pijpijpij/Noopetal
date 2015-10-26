package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import static com.pij.noopetal.GeneratedClassUtil.applyAccessModifier;
import static com.pij.noopetal.GeneratedClassUtil.createGeneratedAnnotation;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * @author Pierrejean on 25/10/2015.
 */
public class DecorClass implements GeneratedClass {

    public static final String DECORATED_FIELD_NAME = "decorated";
    private final String classPackage;
    private final String className;
    private final Class<? extends Processor> processorClass;
    private final TypeElement superType;

    public DecorClass(@NonNull String classPackage, @NonNull String className, @NonNull TypeElement superType,
                      @NonNull Processor processor) {
        this.classPackage = notNull(classPackage);
        this.className = notNull(className);
        this.processorClass = notNull(processor).getClass();
        this.superType = notNull(superType);
    }

    @Override
    public TypeElement getSuperType() {
        return superType;
    }

    @Override
    public String getClassPackage() {
        return classPackage;
    }

    @NonNull
    @Override
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder result = TypeSpec.classBuilder(className);
        applyAccessModifier(superType, result);
        GeneratedClassUtil.applyTypeVariables(superType, result);
        result.addJavadoc(createGeneratedAnnotation(processorClass).toString());
        result.addSuperinterface(getDecoratedTypeName());

        // Add delegate field and constructor
        final FieldSpec decorated = FieldSpec.builder(getDecoratedTypeName(),
                                                      DECORATED_FIELD_NAME,
                                                      Modifier.PRIVATE,
                                                      Modifier.FINAL).build();
        result.addField(decorated);
        result.addMethod(createConstructor());

        for (Element element : superType.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                MethodSpec method = createOverridingMethod((ExecutableElement)element, decorated).build();
                result.addMethod(method);
            }
        }

        return result.build();
    }

    private TypeName getDecoratedTypeName() {
        return TypeName.get(superType.asType());
    }

    /**
     * Code may be sub-optimal.
     */
    private MethodSpec.Builder createOverridingMethod(ExecutableElement element, FieldSpec decorated) {
        final MethodSpec.Builder result = MethodSpec.overriding(element);
        String parameters = "";
        boolean firstParameter = true;
        for (ParameterSpec parameter : result.build().parameters) {
            parameters += parameter.name;
            if (!firstParameter) parameters += ", ";
            else firstParameter = false;
        }
        String format = "$N.$N(" + parameters + ")";
        if (element.getReturnType().getKind() != TypeKind.VOID) {
            format = "return " + format;
        }
        result.addStatement(format, decorated, result.build());

        return result;
    }

    private MethodSpec createConstructor() {
        final String parameterName = "decorated";
        final ParameterSpec.Builder param = ParameterSpec.builder(getDecoratedTypeName(),
                                                                  parameterName,
                                                                  Modifier.FINAL);
        param.addAnnotation(NonNull.class);

        final MethodSpec.Builder result = MethodSpec.constructorBuilder();
        result.addModifiers(Modifier.PUBLIC);
        result.addParameter(param.build());
        result.addStatement("this." + DECORATED_FIELD_NAME + " = " + parameterName);
        return result.build();
    }

}
