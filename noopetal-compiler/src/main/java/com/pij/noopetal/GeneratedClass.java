package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

/**
 * @author Pierrejean on 25/10/2015.
 */
public interface GeneratedClass {

    TypeElement getSuperType();

    String getClassPackage();

    @NonNull
    TypeSpec getTypeSpec();
}
