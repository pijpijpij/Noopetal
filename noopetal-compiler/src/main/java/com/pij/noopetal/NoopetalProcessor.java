package com.pij.noopetal;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;

import java.util.Arrays;

import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;

@AutoService(Processor.class)
public final class NoopetalProcessor extends BasicAnnotationProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        return Arrays.asList(new NoopProcessingStep(getClass(), processingEnv),
                             new DecorProcessingStep(getClass(), processingEnv));
    }

}
