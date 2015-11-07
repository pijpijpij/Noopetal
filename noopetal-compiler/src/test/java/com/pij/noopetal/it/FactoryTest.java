package com.pij.noopetal.it;

import com.google.common.base.Joiner;
import com.pij.noopetal.Noop;
import com.pij.noopetal.NoopetalProcessor;

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
public class FactoryTest {

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
    public void test_emptyPublicInterface_CompilesAndGeneratesPublicInterface() {
        JavaFileObject source = forSourceString("test.Test",
                                                Joiner.on('\n')
                                                      .join("package test;",
                                                            "@com.pij.noopetal.Factory",
                                                            "public interface Test {",
                                                            "}"));
        JavaFileObject expected = forSourceLines("test.TestFactory",
                                                 "package test;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public interface TestFactory {",
                                                 "Test createTest();",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_defaultInterface_CompilesAndGeneratesDefaultInterface() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Factory",
                                               "interface Test {",
                                               "}");
        JavaFileObject expected = forSourceLines("test.TestFactory",
                                                 "package test;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "interface TestFactory {",
                                                 "Test createTest();",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_innerPublicInterface_CompilesAndGenerateUnderscoreInterface() {
        JavaFileObject source = forSourceLines("test.Container",
                                               "package test;",
                                               "public class Container {",
                                               "@com.pij.noopetal.Factory",
                                               "public interface Test {",
                                               "}",
                                               "}");
        JavaFileObject expected = forSourceLines("test/ContainerTestFactory",
                                                 "package test;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public interface Container_TestFactory {",
                                                 "Container.Test createTest();",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_privateInnerPublicInterface_generatesDollarInterfaceButDoesNotCompile() {
        JavaFileObject source = forSourceLines("test.Container",
                                               "package test;",
                                               "public class Container {",
                                               "@com.pij.noopetal.Factory",
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
                                               "@com.pij.noopetal.Factory",
                                               "public class Test {",
                                               "}");
        assertAbout(javaSource()).that(source)
                                 .processedWith(new NoopetalProcessor())
                                 .failsToCompile()
                                 .withErrorContaining("@Factory must only be applied to an interface. Test isn't");
    }

    @Test
    public void test_simpleGenericInterface_CompilesAndGeneratesGenericFactory() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Factory",
                                               "public interface Test<T> {",
                                               "void oneArgMethod(String anArg);",
                                               "}");
        JavaFileObject expected = forSourceLines("test.TestFactory",
                                                 "package test;",
                                                 "/**",
                                                 "* @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public interface TestFactory<T> {",
                                                 "Test<T> createTest();",
                                                 "}");

        assertGeneration(source, expected);
    }

    @Test
    public void test_enumGenericInterface_CompilesAndGeneratesGenericFactory() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Factory",
                                               "public interface Test<T extends Enum<T>> {",
                                               "void oneArgMethod(String anArg);",
                                               "}");
        JavaFileObject expected = forSourceLines("test.TestFactory",
                                                 "package test;",
                                                 "/**",
                                                 "* @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public interface TestFactory<T extends Enum<T>> {",
                                                 "Test<T> createTest();",
                                                 "}");

        assertGeneration(source, expected);
    }

    @Test
    public void test_specifiedFullClassname_CompilesAndGeneratesSpecificiedInterface() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Factory(\"another.pckg.AnotherClass\")",
                                               "public interface Test {",
                                               "}");
        JavaFileObject expected = forSourceLines("another.pckg.AnotherClass",
                                                 "package another.pckg;",
                                                 "import test.Test;",
                                                 "/**",
                                                 "* @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public interface AnotherClass {",
                                                 "Test createTest();",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_specifiedSimpleClassName_CompilesAndGeneratesSpecifiedInterfaceInSamePackage() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Factory(\"AnotherClass\")",
                                               "public interface Test {",
                                               "}");
        JavaFileObject expected = forSourceLines("test.AnotherClass",
                                                 "package test;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public interface AnotherClass {",
                                                 "Test createTest();",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_specifiedDottedClassName_CompilesAndGeneratesSpecificiedInterfaceInSamePackage() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Factory(\".AnotherClass\")",
                                               "public interface Test {",
                                               "}");
        JavaFileObject expected = forSourceLines("test.AnotherClass",
                                                 "package test;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public interface AnotherClass {",
                                                 "Test createTest();",
                                                 "}");
        assertGeneration(source, expected);
    }

    @Test
    public void test_specifiedPackage_CompilesAndGeneratesInSpecificiedPackageWithStandardName() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Factory(\"another.pckg.\")",
                                               "public interface Test {",
                                               "}");
        JavaFileObject expected = forSourceLines("another.pckg.TestFactory",
                                                 "package another.pckg;",
                                                 "import test.Test;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public interface TestFactory {",
                                                 "Test createTest();",
                                                 "}");
        assertGeneration(source, expected);
    }

}