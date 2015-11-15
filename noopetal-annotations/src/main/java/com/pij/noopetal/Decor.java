package com.pij.noopetal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Indicates a decorating implementation of the class is to be provided.
 * <pre><code>
 * {@literal @}Decor
 * interface MyExample {
 *     void doSomething();
 *     void doSomethingElse();
 * }
 * </code></pre>
 * Later the generated class (<code>MyExample<b>Decor</b></code> can be used as the basis to {@link Decor}-ate another
 * instance <code>MyExample</code>:
 * <pre><code>
 * public class AugmentedExample extends MyExampleDecor {
 *   private final MyExample decorated;
 * <p/>
 *   public AugmentedExample(MyExample decorated) {
 *       this.decorated = decorated;
 *   }
 * <p/>
 *   @Override
 *   public void doSomething() {
 *       decorated.doSomething();
 *   }
 * <p/>
 *   //...
 * }
 * </code></pre>
 */
// TODO add @NonNull and notNull check
@Retention(SOURCE)
@Target(TYPE)
public @interface Decor {

    /**
     * Name of the class to generate. If it does not include a package, the package of the annotated interface is used.
     * If it only includes a package (i.e. the last character is a '.', the name of the annotated interface is used,
     * prefixed with <code>"Decorating"</code>.</p> Valid values:<ul> <li><code>com.me.TheClass</code></li>
     * <li><code>com.me.</code></li> <li><code>.TheClass</code></li> <li><code>TheClass</code></li> </ul>
     */
    String value() default "";

    /**
     * If set, the decorated object can be changed: an additional setter is added. It can be called:
     * <pre>
     *     TheClass decorated;
     *     TheClass anotherDecorated;
     *     DecoratingTheClass generated = new DecoratingTheClass(decorated);
     *     // ...
     *     generated.setDecorated(anotherDecorated);
     * </pre>
     */
    boolean mutable() default false;
}
