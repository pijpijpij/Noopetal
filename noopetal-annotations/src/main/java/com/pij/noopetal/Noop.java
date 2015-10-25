package com.pij.noopetal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Indicates a No-operation implementation of the class is to be provided.
 * <pre><code>
 * {@literal @}Noop
 * interface MyExample {
 *     // ..
 * }
 * </code></pre>
 * Later the class can be used:
 * <pre><code>
 * //..
 * private MyExample defaultExample = new MyExampleNoop();
 * </code></pre>
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface Noop { }
