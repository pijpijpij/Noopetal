package com.pij.noopetal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * @author Pierrejean on 25/10/2015.
 */
public class GeneratedClassUtil {

    /**
     * @param typeElement its access modifier is extracted
     * @return the access modifier of the <code>typeElement</code>.
     */
    @Nullable
    public static Modifier extractAccessModifier(@NonNull TypeElement typeElement) {
        final Set<Modifier> modifiers = typeElement.getModifiers();
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

    public static void applyAccessModifier(@NonNull TypeElement source, @NonNull TypeSpec.Builder target) {
        final Modifier modifier = extractAccessModifier(source);
        if (modifier != null) target.addModifiers(modifier);
    }

    @NonNull
    public static AnnotationSpec createGeneratedAnnotation(@NonNull Class<? extends Processor> processorClass) {
        return AnnotationSpec.builder(Generated.class)
                             .addMember("value",
                                        "\"$N\"",
                                        TypeSpec.classBuilder(processorClass.getCanonicalName()).build())
                             .build();
    }

    public static String defaultReturnLiteral(@NonNull TypeMirror returnType) {
        final TypeKind kind = returnType.getKind();
        switch (kind) {
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

    public static void applyTypeVariables(TypeElement source, TypeSpec.Builder target) {
        final List<? extends TypeParameterElement> sourceParameters = source.getTypeParameters();
        for (TypeParameterElement sourceParameter : sourceParameters) {
            TypeVariableName targetParameter = TypeVariableName.get(sourceParameter);
            target.addTypeVariable(targetParameter);
        }
    }

    @NonNull
    public static String calculateGeneratedClassName(TypeElement type, PackageElement packageElement, String prefix) {
        int packageLen = packageElement.getQualifiedName().length() + 1;
        final String simpleClassName = type.getQualifiedName().toString().substring(packageLen);
        String containingClassPrefix = StringUtils.EMPTY;
        final int lastDot = simpleClassName.lastIndexOf('.');
        if (lastDot >= 0) {
            String containingClassName = simpleClassName.substring(0, lastDot);
            containingClassPrefix = containingClassName.replace('.', '_') + "_";
        }
        return containingClassPrefix + prefix + simpleClassName.substring(lastDot + 1);
    }

    @NonNull
    public static Pair<String, String> extractPackageAndClassName(String className) {
        className = defaultString(className);
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0
               ? new ImmutablePair<>(stripToNull(className.substring(0, lastDot)),
                                     stripToNull(className.substring(lastDot + 1)))
               : new ImmutablePair<>((String)null, stripToNull(className));
    }
}
