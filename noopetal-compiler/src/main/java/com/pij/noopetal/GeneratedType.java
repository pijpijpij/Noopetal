package com.pij.noopetal;

import android.support.annotation.NonNull;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

/**
 * @author Pierrejean on 25/10/2015.
 */
interface GeneratedType {

    TypeElement getSourceType();

    String getTypeName();

    String getTypePackage();

    @NonNull
    TypeSpec getTypeSpec();
}
