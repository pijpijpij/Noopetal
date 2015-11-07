package com.pij.noopetal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * @author Pierrejean on 25/10/2015.
 */
class EnrichedTypeElement {

    private final TypeElement typeElement;
    private final Elements elementUtils;

    public EnrichedTypeElement(@NonNull TypeElement typeElement, @NonNull Elements elementUtils) {
        this.typeElement = notNull(typeElement);
        this.elementUtils = notNull(elementUtils);
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public PackageElement getPackage() {
        return elementUtils.getPackageOf(typeElement);
    }

    /**
     * @return the access modifier of the element.
     */
    @Nullable
    private Modifier getAccessModifier() {
        for (Modifier modifier : typeElement.getModifiers()) {
            switch (modifier) {
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                    return modifier;
            }
        }
        return null;
    }

    public void applyAccessModifier(@NonNull TypeSpec.Builder target) {
        final Modifier modifier = getAccessModifier();
        if (modifier != null) target.addModifiers(modifier);
    }

    public void applyTypeVariables(TypeSpec.Builder target) {
        final List<? extends TypeParameterElement> sourceParameters = typeElement.getTypeParameters();
        for (TypeParameterElement sourceParameter : sourceParameters) {
            TypeVariableName targetParameter = TypeVariableName.get(sourceParameter);
            target.addTypeVariable(targetParameter);
        }
    }

    @NonNull
    public String calculateClassNameWithPrefix(String prefix) {
        Pair<String, String> splitName = getSplitSimpleClassName();
        return splitName.getLeft() + prefix + splitName.getRight();
    }

    @NonNull
    public String calculateClassNameWithSuffix(String suffix) {
        Pair<String, String> splitName = getSplitSimpleClassName();
        return splitName.getLeft() + splitName.getRight() + suffix;
    }

    @NonNull
    private Pair<String, String> getSplitSimpleClassName() {
        final String simpleClassName = getSimpleClassName();
        final int lastDot = simpleClassName.lastIndexOf('.');
        String containingClassPrefix = StringUtils.EMPTY;
        if (lastDot >= 0) {
            String containingClassName = simpleClassName.substring(0, lastDot);
            containingClassPrefix = containingClassName.replace('.', '_') + "_";
        }
        final String shortClassName = simpleClassName.substring(lastDot + 1);
        return Pair.of(containingClassPrefix, shortClassName);
    }

    @NonNull
    private String getSimpleClassName() {
        int packageLen = getPackage().getQualifiedName().length() + 1;
        return typeElement.getQualifiedName().toString().substring(packageLen);
    }

    public List<? extends Element> getEnclosedElements() {
        return typeElement.getEnclosedElements();
    }

    public TypeMirror asType() {
        return typeElement.asType();
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return typeElement.getAnnotation(annotationType);
    }

}
