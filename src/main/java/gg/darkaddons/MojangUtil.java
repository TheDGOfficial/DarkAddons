package gg.darkaddons;

import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

final class MojangUtil {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private MojangUtil() {
        super();

        throw Utils.staticClassException();
    }

    static final void getUUIDFromUsername(@NotNull final String username, @NotNull final Consumer<UUID> successCallback, @NotNull final Consumer<Throwable> errorCallback) {
        gg.skytils.skytilsmod.utils.MojangUtil.INSTANCE.getUUIDFromUsername(username, new MojangUtil.JavaContinuation<>(successCallback, errorCallback));
    }

    private static final class JavaContinuation<T> implements Continuation<T> {
        @NotNull
        private final Consumer<? super T> onSuccess;

        @NotNull
        private final Consumer<? super Throwable> onFailure;

        private JavaContinuation(@NotNull final Consumer<? super T> onSuccessIn, @NotNull final Consumer<? super Throwable> onFailureIn) {
            super();

            this.onSuccess = onSuccessIn;
            this.onFailure = onFailureIn;
        }

        @NotNull
        @Override
        public final CoroutineContext getContext() {
            return EmptyCoroutineContext.INSTANCE;
        }

        @SuppressWarnings("unchecked")
        @Override
        public final void resumeWith(@NotNull final Object o) {
            if (o instanceof Result.Failure) {
                this.onFailure(((Result.Failure) o).exception);
            } else {
                this.onSuccess((T) o);
            }
        }

        private final void onSuccess(@NotNull final T o) {
            this.onSuccess.accept(o);
        }

        private final void onFailure(@NotNull final Throwable exception) {
            this.onFailure.accept(exception);
        }

        @Override
        public final String toString() {
            return "JavaContinuation{" +
                "onSuccess=" + this.onSuccess +
                ", onFailure=" + this.onFailure +
                '}';
        }
    }
}
