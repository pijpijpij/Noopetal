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
public @interface Noop {

    /**
     * Name of the class to generate. If it does not include a package, the package of the annotated interface is used.
     * If it only includes a package (i.e. the last character is a '.', the name of the annotated interface is used,
     * prefixed with <code>"Noop"</code>.</p> Valid values:<ul> <li><code>com.me.TheClass</code></li>
     * <li><code>com.me.</code></li> <li><code>.TheClass</code></li> <li><code>TheClass</code></li> </ul>
     */
    String value() default "";
}
