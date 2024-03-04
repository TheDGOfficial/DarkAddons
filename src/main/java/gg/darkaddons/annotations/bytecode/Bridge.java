package gg.darkaddons.annotations.bytecode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation for our custom bytecode processor.
 * <p>
 * If any method or constructor is marked with this, it will be
 * marked as bridge in the JVM bytecode.
 * <p>
 * Bridge methods will be hidden in most decompilers.
 * Use together with {@link Synthetic} to increase the chance of
 * hiding.
 * <p>
 * Do note that the method is still available fully and can be decompiled
 * easily. This is just exploiting a behavior on decompilers to hide your
 * bad code. Use for when you are ashamed of the code of some method,
 * but it's the best-performance way of doing it.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(/*{*/ElementType.METHOD/*, ElementType.CONSTRUCTOR}*/)
public @interface Bridge {
}
