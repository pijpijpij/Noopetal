package com.pij.noopetal;

import com.google.common.base.Joiner;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Integration test for th {@link Noop} annotation
 * @author Pierrejean on 24/10/2015.
 */
public class NoopTest {

    private static final String EMPTY_INTERFACE = Joiner.on('\n')
                                                        .join("package test;",
                                                              "import com.pij.noopetal.Noop;",
                                                              "@Noop",
                                                              "public interface Test {",
                                                              "}");

    private static void assertGeneration(JavaFileObject source, JavaFileObject expectedGeneratedSource) {
        assertAbout(javaSource()).that(source)
                                 .processedWith(new NoopetalProcessor())
                                 .compilesWithoutError()
                                 .and()
                                 .generatesSources(expectedGeneratedSource);
    }

    @Test
    public void test_nonAnnotatedInterface_compilesButDoesNotGenerate() {
        JavaFileObject source = forSourceLines("test.Test", "package test;", "public interface Test {", "}");

        assertAbout(javaSource()).that(source).processedWith(new NoopetalProcessor()).compilesWithoutError();
    }

    @Test
    public void test_emptyPublicInterface_CompilesAndGeneratesPublicClass() {
        JavaFileObject source = forSourceString("test.Test", EMPTY_INTERFACE);
        JavaFileObject expected = forSourceLines("test/TestNoop",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class TestNoop implements Test {",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_defaultInterface_CompilesAndGeneratesDefaultClass() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "import com.pij.noopetal.Noop;",
                                               "@Noop",
                                               "interface Test {",
                                               "}");
        JavaFileObject expected = forSourceLines("test/TestNoop",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "class TestNoop implements Test {",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_innerPublicInterface_CompilesAndGenerateDollarClass() {
        JavaFileObject source = forSourceLines("test.Container",
                                               "package test;",
                                               "import com.pij.noopetal.Noop;",
                                               "public class Container {",
                                               "@Noop",
                                               "public interface Test {",
                                               "}",
                                               "}");
        JavaFileObject expected = forSourceLines("test/Container$TestNoop",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class Container$TestNoop implements Container.Test {",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_privateInnerPublicInterface_generatesDollarClassButDoesNotCompile() {
        JavaFileObject source = forSourceLines("test.Container",
                                               "package test;",
                                               "import com.pij.noopetal.Noop;",
                                               "public class Container {",
                                               "@Noop",
                                               "private interface Test {",
                                               "}",
                                               "}");
        assertAbout(javaSource()).that(source)
                                 .processedWith(new NoopetalProcessor())
                                 .failsToCompile()
                                 .withErrorContaining("Test has private access in test.Container");
    }

}