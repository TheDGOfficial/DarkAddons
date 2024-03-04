package gg.darkaddons;

/**
 * This interface defines a Runnable that can throw checked (and unchecked) exceptions.
 * <p>
 * This class doesn't extend {@link Runnable} as the throw clause of the {@link CheckedThrowingRunnable#run()}
 * method and {@link Runnable#run()} method doesn't match.
 * <p>
 * Use {@link ThrowingRunnable#fromChecked(CheckedThrowingRunnable)} if you want to wrap it as a
 * {@link ThrowingRunnable}, which extends from {@link Runnable}. See the docs on {@link ThrowingRunnable}
 * about how the exception is handled in that case.
 *
 * @param <T> The type of the exception that can the {@link CheckedThrowingRunnable#run()} method
 *           throw. This must be a super-type of all the (checked) exceptions that can be thrown.
 */
@FunctionalInterface
interface CheckedThrowingRunnable<T extends Exception> {
    /**
     * Runs this operation, that can throw a checked exception.
     *
     * @throws T A checked exception. The thrown (checked) exceptions must be this type
     * or one of its subclasses, unless the contracts to the Java throw clause are broken with
     * methods such as {@link JavaUtils#sneakyThrow(Throwable)}, in which case the thrown exception
     * can be of any type (that extends from {@link Throwable}).
     */
    void run() throws T;
}
