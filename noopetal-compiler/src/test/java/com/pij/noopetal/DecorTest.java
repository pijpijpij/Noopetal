package com.pij.noopetal;

import com.google.common.base.Joiner;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Integration test for the {@link Decoration} annotation.
 * @author Pierrejean on 24/10/2015.
 */
public class DecorTest {

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
    public void test_emptyPublicInterface_GeneratesEmptyClass() {
        JavaFileObject source = forSourceString("test.Test",
                                                Joiner.on('\n')
                                                      .join("package test;",
                                                            "@com.pij.noopetal.Decor",
                                                            "public interface Test {",
                                                            "}"));
        JavaFileObject expected = forSourceLines("test/DecoratingTest",
                                                 "package test;",
                                                 "",
                                                 "import android.support.annotation.NonNull;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class DecoratingTest implements Test {",
                                                 "",
                                                 "private final Test decorated;",
                                                 "",
                                                 "public DecoratingTest(@NonNull final Test decorated) {",
                                                 "this.decorated = decorated;",
                                                 "}",
                                                 "}");
        assertGeneration(source, expected);

    }

    @Test
    public void test_defaultInterface_GeneratesEmptyClass() {
        JavaFileObject source = forSourceString("test.Test",
                                                Joiner.on('\n')
                                                      .join("package test;",
                                                            "@com.pij.noopetal.Decor",
                                                            "interface Test {",
                                                            "}"));
        JavaFileObject expected = forSourceLines("test/DecoratingTest",
                                                 "package test;",
                                                 "",
                                                 "import android.support.annotation.NonNull;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "class DecoratingTest implements Test {",
                                                 "",
                                                 "private final Test decorated;",
                                                 "",
                                                 "public DecoratingTest(@NonNull final Test decorated) {",
                                                 "this.decorated = decorated;",
                                                 "}",
                                                 "}");
        assertGeneration(source, expected);

    }

    @Test
    public void test_privateInnerInterface_FailsToCompile() {
        JavaFileObject source = forSourceLines("test.Container",
                                               "package test;",
                                               "public class Container {",
                                               "@com.pij.noopetal.Decor",
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
                                               "@com.pij.noopetal.Decor",
                                               "public class Test {",
                                               "}");
        assertAbout(javaSource()).that(source)
                                 .processedWith(new NoopetalProcessor())
                                 .failsToCompile()
                                 .withErrorContaining("@Decor must only be applied to an interface. Test isn't");
    }


}
