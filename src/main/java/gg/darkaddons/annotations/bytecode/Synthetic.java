package gg.darkaddons.annotations.bytecode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation for our custom bytecode processor.
 * <p>
 * If any class, method or constructor is marked with this, it will be
 * marked as synthetic in the JVM bytecode.
 * <p>
 * Methods marked synthetic are hidden in JD-GUI decompiler, but will still
 * show in FernFlower.
 * <p>
 * However, if you additionally make it {@link Bridge} then FernFlower will hide
 * it too.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(/*{ElementType.TYPE, */ElementType.METHOD/*, ElementType.CONSTRUCTOR}*/)
public @interface Synthetic {
}
