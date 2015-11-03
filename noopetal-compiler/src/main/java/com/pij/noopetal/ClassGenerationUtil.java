package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * @author Pierrejean on 25/10/2015.
 */
class ClassGenerationUtil {

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
