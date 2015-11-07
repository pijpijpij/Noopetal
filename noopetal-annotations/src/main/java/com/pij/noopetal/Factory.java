package com.pij.noopetal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Indicates a factory interface for the marked interface is to be provided.
 * <pre><code>
 * {@literal @}Factory
 * interface MyExample {
 *     // ..
 * }
 * </code></pre>
 * Later the interface <code>MyExampleFactory</code> can be used:
 * <pre><code>
 * //..
 * private MyExampleFactory factory;
 * // ..
 * MyExample defaultExample = factory.createMyExample();
 * </code></pre>
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface Factory {

    /**
     * Name of the interface to generate. If it does not include a package, the package of the annotated interface is
     * used. If it only includes a package (i.e. the last character is a '.', the name of the annotated interface is
     * used, <em>suffixed</em> with <code>"Factory"</code>.</p> Valid values:<ul> <li><code>com.me.TheFactory</code></li>
     * <li><code>com.me.</code></li> <li><code>.TheFactory</code></li> <li><code>TheFactory</code></li> </ul>
     */
    String value() default "";
}
