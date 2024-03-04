package gg.darkaddons.annotations.bytecode;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation for our custom bytecode processor.
 * <p>
 * If any class, method or constructor is marked with this, it will be
 * renamed to the given name in the JVM bytecode.
 * <p>
 * The calls to the method with old name will NOT be fixed.
 * <p>
 * This can be used to have methods with names restricted in the Java language
 * but allowed in the bytecode (such as this [reserved keyword], or method names with spaces)
 * <p>
 * It can also be used to make two methods have same name, same arguments but different
 * return types, and since Java bytecode calling the methods always use fully qualified
 * signatures, the binary compatibility will be preserved, although there will be ambiguity
 * on which method to call when compiling the code, so consider marking one of the methods
 * with {@link Synthetic} to hide it from using in Java code.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(/*{ElementType.TYPE, */ElementType.METHOD/*, ElementType.CONSTRUCTOR}*/)
public @interface Name {
    /**
     * Gets the name of this method in runtime.
     *
     * @return The name of this method in runtime.
     */
    @NotNull
    String value();
}
