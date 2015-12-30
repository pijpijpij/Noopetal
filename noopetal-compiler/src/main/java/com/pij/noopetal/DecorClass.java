package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import static com.pij.noopetal.ClassGenerationUtil.createGeneratedAnnotation;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * @author Pierrejean on 25/10/2015.
 */
class DecorClass implements GeneratedType {

    private static final String DECORATED_FIELD_NAME = "decorated";

    private final String classPackage;
    private final String className;
    private final Class<? extends Processor> processorClass;
    private final EnrichedTypeElement sourceType;
    private final boolean mutable;

    public DecorClass(@NonNull String classPackage, @NonNull String className, @NonNull EnrichedTypeElement sourceType,
                      @NonNull Class<? extends Processor> processorClass, boolean mutable) {
        this.mutable = mutable;
        this.classPackage = notNull(classPackage);
        this.className = notNull(className);
        this.processorClass = notNull(processorClass);
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

        // Add delegate field and constructor
        final FieldSpec decorated = FieldSpec.builder(getDecoratedTypeName(), DECORATED_FIELD_NAME, getFieldModifiers())
                                             .build();
        result.addField(decorated);
        result.addMethod(createConstructor());
        if (mutable) result.addMethod(createSetter());

        // Extracts methods elements and generate code from the element list.
        for (Element element : sourceType.getAllEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                MethodSpec item = createOverridingMethod((ExecutableElement)element, decorated).build();
                result.addMethod(item);
            }
        }

        return result.build();
    }

    @NonNull
    private Modifier[] getFieldModifiers() {
        Modifier[] result = new Modifier[]{ Modifier.PRIVATE };
        if (!mutable) result = ArrayUtils.add(result, Modifier.FINAL);

        return result;
    }

    private TypeName getDecoratedTypeName() {
        return TypeName.get(sourceType.asType());
    }

    /**
     * Code may be sub-optimal.
     */
    private MethodSpec.Builder createOverridingMethod(ExecutableElement element, FieldSpec decorated) {
        final MethodSpec.Builder result = MethodSpec.overriding(element);
        String parameters = "";
        boolean firstParameter = true;
        for (ParameterSpec parameter : result.build().parameters) {
            if (!firstParameter) parameters += ", ";
            else firstParameter = false;
            parameters += parameter.name;
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

    private MethodSpec createSetter() {
        final String parameterName = "newValue";
        final ParameterSpec.Builder param = ParameterSpec.builder(getDecoratedTypeName(),
                                                                  parameterName,
                                                                  Modifier.FINAL);
        param.addAnnotation(NonNull.class);

        final MethodSpec.Builder result = MethodSpec.methodBuilder("setDecorated");
        result.addModifiers(Modifier.PUBLIC);
        result.addParameter(param.build());
        result.addStatement("this." + DECORATED_FIELD_NAME + " = " + parameterName);
        return result.build();
    }

}
