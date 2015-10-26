package com.pij.noopetal;

import com.google.common.base.Joiner;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Integration test for the {@link Noop} annotation
 * @author Pierrejean on 24/10/2015.
 */
public class NoopTest {

    private static void assertGeneration(JavaFileObject source, JavaFileObject expectedGeneratedSource) {
        assertAbout(javaSource()).that(source)
                                 .processedWith(new NoopetalProcessor())
                                 .compilesWithoutError()
                                 .and()
                                 .generatesSources(expectedGeneratedSource);
    }

    @Test
    public void test_nonAnnotatedInterface_compiles() {
        JavaFileObject source = forSourceLines("test.Test", "package test;", "public interface Test {", "}");

        assertAbout(javaSource()).that(source).processedWith(new NoopetalProcessor()).compilesWithoutError();
    }

    @Test
    public void test_emptyPublicInterface_CompilesAndGeneratesPublicClass() {
        JavaFileObject source = forSourceString("test.Test",
                                                Joiner.on('\n')
                                                      .join("package test;",
                                                            "@com.pij.noopetal.Noop",
                                                            "public interface Test {",
                                                            "}"));
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class NoopTest implements Test {",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_defaultInterface_CompilesAndGeneratesDefaultClass() {
        JavaFileObject source = forSourceLines("test.Test", "package test;", "@com.pij.noopetal.Noop",
                                               "interface Test {",
                                               "}");
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "class NoopTest implements Test {",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_innerPublicInterface_CompilesAndGenerateDollarClass() {
        JavaFileObject source = forSourceLines("test.Container",
                                               "package test;", "public class Container {", "@com.pij.noopetal.Noop",
                                               "public interface Test {",
                                               "}",
                                               "}");
        JavaFileObject expected = forSourceLines("test/Container$NoopTest",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class Container$NoopTest implements Container.Test {",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_privateInnerPublicInterface_generatesDollarClassButDoesNotCompile() {
        JavaFileObject source = forSourceLines("test.Container",
                                               "package test;", "public class Container {", "@com.pij.noopetal.Noop",
                                               "private interface Test {",
                                               "}",
                                               "}");
        assertAbout(javaSource()).that(source)
                                 .processedWith(new NoopetalProcessor())
                                 .failsToCompile()
                                 .withErrorContaining("Test has private access in test.Container");
    }

    @Test
    public void test_publicClass_doesNotCompile() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Noop",
                                               "public class Test {",
                                               "}");
        assertAbout(javaSource()).that(source)
                                 .processedWith(new NoopetalProcessor())
                                 .failsToCompile()
                                 .withErrorContaining("@Noop must only be applied to an interface. Test isn't");
    }

}