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
 * Integration test for the {@link Decor} annotation.
 * @author Pierrejean on 24/10/2015.
 */
public class DecorTest {

    private static final String[] STANDARD_SOURCE_HEADER = {
            "package test;", "@com.pij.noopetal.Decor", "public interface Test {"
    };

    private static final String[] STANDARD_EXPECTED_HEADER = {
            "package test;",
            "",
            "import android.support.annotation.NonNull;",
            "",
            "/**",
            " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
            "public class DecoratingTest implements Test {",
            "",
            "private final Test decorated;",
            "",
            "public DecoratingTest(@NonNull final Test decorated) {",
            "this.decorated = decorated;",
            "}",
            ""
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
    public void test_innerPublicInterface_CompilesAndGenerateUnderscoreClass() {
        JavaFileObject source = forSourceLines("test.Container",
                                               "package test;",
                                               "public class Container {",
                                               "@com.pij.noopetal.Decor",
                                               "public interface Test {",
                                               "}",
                                               "}");
        JavaFileObject expected = forSourceLines("test/Container_DecoratingTest",
                                                 "package test;",
                                                 "import android.support.annotation.NonNull;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class Container_DecoratingTest implements Container.Test {",
                                                 "private final Container.Test decorated;",
                                                 "public DecoratingTest(@NonNull final Container.Test decorated) {",
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

    @Test
    public void test_methodVoidWithNoArguments_CompilesAndGeneratesCallsDecorated() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER, "void noArgVoidMethod();", "}")));
        JavaFileObject expected = forSourceLines("test/NoopTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public void noArgVoidMethod() {",
                                                               "decorated.noArgVoidMethod();",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodWithReturnValueWithNoArguments_CompilesAndGeneratesReturnsDecoratedCall() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER, "long noArgMethod();", "}")));
        JavaFileObject expected = forSourceLines("test/DecoratingTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public long noArgMethod() {",
                                                               "return decorated.noArgMethod();",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodWithOneArgument_CompilesAndGeneratesPassesArgumentToDecorated() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER,
                                                             "void oneArgMethod(String anArg);",
                                                             "}")));
        JavaFileObject expected = forSourceLines("test/DecoratingTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public void oneArgMethod(String anArg) {",
                                                               "decorated.oneArgMethod(anArg);",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodWithOneVarargArgument_CompilesAndGeneratesPassesArgumentToDecorated() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER,
                                                             "void oneArgMethod(String... anArg);",
                                                             "}")));
        JavaFileObject expected = forSourceLines("test/DecoratingTest",
                                                 asList(addAll(STANDARD_EXPECTED_HEADER,
                                                               "@Override",
                                                               "public void oneArgMethod(String... anArg) {",
                                                               "decorated.oneArgMethod(anArg);",
                                                               "}",
                                                               "}")));

        assertGeneration(source, expected);
    }

    @Test
    public void test_methodWithOneGenericArgument_CompilesAndGeneratesPassesArgumentToDecorated() {
        JavaFileObject source = forSourceLines("test.Test",
                                               asList(addAll(STANDARD_SOURCE_HEADER,
                                                             "void oneArgMethod(java.util.List<String> anArg);",
                                                             "}")));
        JavaFileObject expected = forSourceLines("test/DecoratingTest",
                                                 "package test;",
                                                 "",
                                                 "import android.support.annotation.NonNull;",
                                                 "import java.util.List;",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class DecoratingTest implements Test {",
                                                 "",
                                                 "private final Test decorated;",
                                                 "",
                                                 "public DecoratingTest(@NonNull final Test decorated) {",
                                                 "this.decorated = decorated;",
                                                 "}",
                                                 "",
                                                 "@Override",
                                                 "public void oneArgMethod(List<String> anArg) {",
                                                 "decorated.oneArgMethod(anArg);",
                                                 "}",
                                                 "}");

        assertGeneration(source, expected);
    }

    @Test
    public void test_simpleGenericInterface_CompilesAndGeneratesGenericDecorator() {
        JavaFileObject source = forSourceLines("test.Test",
                                               "package test;",
                                               "@com.pij.noopetal.Decor",
                                               "public interface Test<T> {",
                                               "void oneArgMethod(String anArg);",
                                               "}");
        JavaFileObject expected = forSourceLines("test/DecoratingTest",
                                                 "package test;",
                                                 "",
                                                 "import android.support.annotation.NonNull;",
                                                 "",
                                                 "/**",
                                                 " * @javax.annotation.Generated(\"com.pij.noopetal.NoopetalProcessor\") */",
                                                 "public class DecoratingTest<T> implements Test<T> {",
                                                 "",
                                                 "private final Test<T> decorated;",
                                                 "",
                                                 "public DecoratingTest(@NonNull final Test<T> decorated) {",
                                                 "this.decorated = decorated;",
                                                 "}",
                                                 "",
                                                 "@Override",
                                                 "public void oneArgMethod(String anArg) {",
                                                 "decorated.oneArgMethod(anArg);",
                                                 "}",
                                                 "}");

        assertGeneration(source, expected);
    }

}
