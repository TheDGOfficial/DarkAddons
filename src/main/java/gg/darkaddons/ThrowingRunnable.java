package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import gg.darkaddons.annotations.bytecode.Private;

/**
 * An interface for {@link Runnable}s that can throw exceptions.
 * <p>
 * This a {@link FunctionalInterface}, thus can be used as a lambda
 * or method reference. This is recommended over using anonymous classes
 * implementing this interface for memory reasons, because if the lambda
 * or method reference doesn't capture anything, the JVM only uses one
 * instance, so the Garbage Collector's job is easier.
 * <p>
 * This interface extends {@link Runnable} for convenience, so you can pass
 * your lambdas/method references implementing this type to methods
 * accepting a {@link Runnable} only.
 */
@FunctionalInterface
interface ThrowingRunnable extends Runnable {
    /**
     * Runs this {@link Runnable}, that can throw unchecked exceptions.
     * <p>
     * This method can't throw checked exceptions.
     * <p>
     * Use {@link CheckedThrowingRunnable} and wrap it to a throwing one
     * if you really need, by using {@link ThrowingRunnable#fromChecked(CheckedThrowingRunnable)}.
     */
    void runThrowing();

    /**
     * {@inheritDoc}
     *
     * This method uses {@link JavaUtils#sneakyThrow(Throwable)} to re-throw any errors
     * as unchecked errors.
     */
    @Override
    default void run() {
        this.runHandling(ThrowingRunnable::handleDefault);
    }

    /**
     * Runs this throwing-runnable with the given error handler.
     * <p>
     * The handler's {@link Consumer#accept(Object)} method will be called with the
     * error as parameter if an error happens.
     * <p>
     * This method only calls the handler if an error occurs and does not do any
     * logging or handling of the error itself, therefore, the given handler should
     * deal with the error properly.
     *
     * @param handler The handler to run the throwing-runnable with.
     */
    default void runHandling(@NotNull final Consumer<? super Throwable> handler) {
        try {
            this.runThrowing();
        } catch (final Throwable error) {
            handler.accept(error);
        }
    }

    /**
     * Handles the passed error with default handler, that is, the {@link JavaUtils#sneakyThrow(Throwable)}.
     * This method merely exists so that we don't duplicate code, and if we switch to another way in the future,
     * we only need to change it in one place.
     * <p>
     * This method is private in runtime, so it should NOT be used from outside this class.
     *
     * @param error The error to handle with the default handler.
     */
    @Private
    static void handleDefault(@NotNull final Throwable error) {
        JavaUtils.sneakyThrow(error);
    }

    /**
     * Convenience method to create throwing-runnable.
     *
     * @param throwingRunnable A lambda or method reference that can throw (unchecked) exceptions.
     * @return The throwing-runnable instance.
     */
    @NotNull
    static ThrowingRunnable of(@NotNull final ThrowingRunnable throwingRunnable) {
        return throwingRunnable;
    }

    /**
     * Creates throwing-runnable from runnable.
     *
     * @param runnable The runnable.
     * @return The throwing-runnable.
     */
    @NotNull
    static ThrowingRunnable fromRunnable(@NotNull final Runnable runnable) {
        return runnable::run;
    }

    /**
     * Creates throwing-runnable from {@link CheckedThrowingRunnable}.
     * <p>
     * This method will re-throw any checked exceptions that occur in the given {@link CheckedThrowingRunnable},
     * which then can be further handled by {@link ThrowingRunnable#runHandling(Consumer)}.
     * <p>
     * If you run the returned throwing-runnable with {@link ThrowingRunnable#run()} instead of {@link ThrowingRunnable#runHandling(Consumer)}
     * then the exception will just propagate upwards, likely being caught by the {@link Thread}s {@link Thread.UncaughtExceptionHandler}.
     *
     * @param checkedThrowingRunnable The {@link CheckedThrowingRunnable}.
     * @return The throwing-runnable.
     */
    @NotNull
    static ThrowingRunnable fromChecked(@NotNull final CheckedThrowingRunnable<?> checkedThrowingRunnable) {
        return () -> {
            try {
                checkedThrowingRunnable.run();
            } catch (final Exception e) {
                ThrowingRunnable.handleDefault(e);
            }
        };
    }
}
