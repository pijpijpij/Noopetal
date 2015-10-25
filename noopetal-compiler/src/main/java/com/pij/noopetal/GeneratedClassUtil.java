package com.pij.noopetal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Pierrejean on 25/10/2015.
 */
public class GeneratedClassUtil {

    /**
     * @param typeElement its access modifier is extracted
     * @return the access modifier of the <code>typeElement</code>.
     */
    @Nullable
    public static Modifier extractAccessModifier(TypeElement typeElement) {
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

    public static void addAccessModifier(TypeSpec.Builder target, TypeElement source) {
        final Modifier modifier = extractAccessModifier(source);
        if (modifier != null) target.addModifiers(modifier);
    }

    @NonNull
    public static AnnotationSpec createGeneratedAnnotation(Class<? extends Processor> processorClass) {
        return AnnotationSpec.builder(Generated.class)
                             .addMember("value",
                                        "\"$N\"",
                                        TypeSpec.classBuilder(processorClass.getCanonicalName()).build())
                             .build();
    }

    public static String calculateDefaultLiteral(TypeMirror returnType) {
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
}
