package gg.darkaddons.annotations.bytecode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation for our custom bytecode processor.
 * <p>
 * If any method, constructor or field is marked with this, it will be
 * marked as private in the JVM bytecode.
 * <p>
 * This can be used to make a method private in interfaces in Java 8,
 * like on Java 9 and above.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(/*{*/ElementType.METHOD/*, ElementType.CONSTRUCTOR, ElementType.FIELD}*/)
public @interface Private {
}
