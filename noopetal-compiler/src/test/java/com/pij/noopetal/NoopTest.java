package com.pij.noopetal;

import com.google.common.base.Joiner;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.addAll;

/**
 * Integration test for the {@link Noop} annotation
 * @author Pierrejean on 24/10/2015.
 */
public class NoopTest {

    private static final String[] STANDARD_SOURCE_HEADER = {
            "package test;", "@com.pij.noopetal.Noop", "public interface Test {"
    };

    private static final String[] STANDARD_EXPECTED_HEADER = {
            "package test;", "", "/**", " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
            "public class NoopTest implements Test {"
    };

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
                                               "package test;",
                                               "public class Container {",
                                               "@com.pij.noopetal.Noop",
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
                                               "package test;",
                                               "public class Container {",
                                               "@com.pij.noopetal.Noop",
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

    @Test
    public void test_methodVoidWithNoArguments_CompilesAndGenerates() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER, "void noArgVoidMethod();", "}")));
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public void noArgVoidMethod() {",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodLongWithNoArguments_CompilesAndGeneratesZeroReturnValue() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER, "long noArgMethod();", "}")));
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public long noArgMethod() {",
                                                               "return 0L;",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodIntegerWithNoArgument_CompilesAndGeneratesNullReturnValue() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER, "Integer noArgMethod();", "}")));
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public Integer noArgMethod() {",
                                                               "return null;",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodWithOneArgument_CompilesAndGenerates() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER,
                                                             "void oneArgMethod(String anArg);",
                                                             "}")));
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public void oneArgMethod(String anArg) {",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodWithOneVarargArgument_CompilesAndGenerates() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER,
                                                             "void oneArgMethod(String... anArg);",
                                                             "}")));
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public void oneArgMethod(String... anArg) {",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodWithOneGenericArgument_CompilesAndGenerates() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER,
                                                             "void oneArgMethod(java.util.List<String> anArg);",
                                                             "}")));
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 "package test;",
                                                 "",
                                                 "import java.util.List;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class NoopTest implements Test {",
                                                 "@Override",
                                                 "public void oneArgMethod(List<String> anArg) {",
                                                 "}",
                                                 "}");

        assertGeneration(source, expected);
    }

    @Test
    public void test_simpleGenericInterface_CompilesAndGeneratesGenericNoop() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Noop",
                                               "public interface Test<T> {",
                                               "void oneArgMethod(String anArg);",
                                               "}");
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class NoopTest<T> implements Test<T> {",
                                                 "@Override",
                                                 "public void oneArgMethod(String anArg) {",
                                                 "}",
                                                 "}");

        assertGeneration(source, expected);
    }

    @Test
    public void test_enumGenericInterface_CompilesAndGeneratesGenericNoop() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Noop", "public interface Test<T extends Enum<T>> {",
                                               "void oneArgMethod(String anArg);",
                                               "}");
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class NoopTest<T extends Enum<T>> implements Test<T> {",
                                                 "@Override",
                                                 "public void oneArgMethod(String anArg) {",
                                                 "}",
                                                 "}");

        assertGeneration(source, expected);
    }

    @Test
    public void test_enumGenericMethod_CompilesAndGeneratesGenericMethod() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Noop",
                                               "public interface Test {",
                                               "<T extends Enum<T>> T oneArgMethod(T anArg);",
                                               "}");
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 "package test;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class NoopTest implements Test {",
                                                 "@Override",
                                                 "public <T extends Enum<T>> T oneArgMethod(T anArg) {",
                                                 "return null;",
                                                 "}",
                                                 "}");

        assertGeneration(source, expected);
    }

}