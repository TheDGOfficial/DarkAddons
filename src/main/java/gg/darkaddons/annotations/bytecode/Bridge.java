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
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(/*{*/ElementType.METHOD/*, ElementType.CONSTRUCTOR}*/)
public @interface Bridge {
}
