package gg.darkaddons.mixin;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * Various common methods used in Mixin are grouped in this
 * class to avoid duplicating code.
 */
public final class MixinUtils {
    private static boolean jumpOverride;

    public static final boolean isJumpOverride() {
        return MixinUtils.jumpOverride;
    }

    public static final void setJumpOverride(final boolean newJumpOverride) {
        MixinUtils.jumpOverride = newJumpOverride;
    }

    @NotNull
    private static BooleanSupplier jumpOverridePrecondition = () -> false;

    @NotNull
    public static final BooleanSupplier getJumpOverridePrecondition() {
        return MixinUtils.jumpOverridePrecondition;
    }

    public static final void setJumpOverridePrecondition(@NotNull final BooleanSupplier newJumpOverridePrecondition) {
        MixinUtils.jumpOverridePrecondition = newJumpOverridePrecondition;
    }

    private static boolean sneakOverride;

    public static final boolean isSneakOverride() {
        return MixinUtils.sneakOverride;
    }

    public static final void setSneakOverride(final boolean newSneakOverride) {
        MixinUtils.sneakOverride = newSneakOverride;
    }

    @NotNull
    private static BooleanSupplier sneakOverridePrecondition = () -> false;

    @NotNull
    public static final BooleanSupplier getSneakOverridePrecondition() {
        return MixinUtils.sneakOverridePrecondition;
    }

    public static final void setSneakOverridePrecondition(@NotNull final BooleanSupplier newSneakOverridePrecondition) {
        MixinUtils.sneakOverridePrecondition = newSneakOverridePrecondition;
    }

    private static boolean punchOverride;

    public static final boolean isPunchOverride() {
        return MixinUtils.punchOverride;
    }

    public static final void setPunchOverride(final boolean newPunchOverride) {
        MixinUtils.punchOverride = newPunchOverride;
    }

    @NotNull
    private static BooleanSupplier punchOverridePrecondition = () -> false;

    @NotNull
    public static final BooleanSupplier getPunchOverridePrecondition() {
        return MixinUtils.punchOverridePrecondition;
    }

    public static final void setPunchOverridePrecondition(@NotNull final BooleanSupplier newPunchOverridePrecondition) {
        MixinUtils.punchOverridePrecondition = newPunchOverridePrecondition;
    }

    private static int lastTicksRan;

    public static final void setLastTicksRan(final int newLastTicksRan) {
        MixinUtils.lastTicksRan = newLastTicksRan;
    }

    public static final int getLastTicksRan() {
        return MixinUtils.lastTicksRan;
    }

    private static long elapsedTicksStart;

    public static final void setElapsedTicksStart(final long newElapsedTicksStart) {
        MixinUtils.elapsedTicksStart = newElapsedTicksStart;
    }

    public static final long getElapsedTicksStart() {
        return MixinUtils.elapsedTicksStart;
    }

    private static long elapsedTicksEnd;

    public static final void setElapsedTicksEnd(final long newElapsedTicksEnd) {
        MixinUtils.elapsedTicksEnd = newElapsedTicksEnd;
    }

    public static final long getElapsedTicksEnd() {
        return MixinUtils.elapsedTicksEnd;
    }

    private MixinUtils() {
        super();
    }

    /**
     * Returns a new {@link IllegalStateException} to throw
     * when a {@link org.spongepowered.asm.mixin.Shadow} annotated
     * method is tried to be invoked directly, i.e., when shadowing
     * fails.
     *
     * @return A new {@link IllegalStateException} with a message
     * that explains the shadowing process is failed.
     */
    @NotNull
    public static final IllegalStateException shadowFail() {
        return new IllegalStateException("shadow failed");
    }

    public static final class IllegalArgumentExceptionHolder {
        private IllegalArgumentExceptionHolder() {
            super();
        }

        @NotNull
        private static final IllegalArgumentException INSTANCE = new IllegalArgumentException("Tried to create a duplicate team (stacktrace below is cached for the first duplicate team creation)");

        /**
         * Gets the instance.
         *
         * @return The instance.
         */
        @NotNull
        public static final IllegalArgumentException getInstance() {
            return MixinUtils.IllegalArgumentExceptionHolder.INSTANCE;
        }
    }
}
