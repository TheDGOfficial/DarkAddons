package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class NameTagCache {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private NameTagCache() {
        super();

        throw Utils.staticClassException();
    }

    @Nullable
    private static String lastNameTag;

    static final void setLastNameTag(@NotNull final String newLastNameTag) {
        //noinspection IfCanBeAssertion,VariableNotUsedInsideIf
        if (null != NameTagCache.lastNameTag) {
            throw new IllegalStateException("clear not called before a new set");
        }
        NameTagCache.lastNameTag = newLastNameTag;
    }

    @Nullable
    static final String getLastNameTag() {
        return NameTagCache.lastNameTag;
    }

    static final void clearLastNameTag() {
        NameTagCache.lastNameTag = null;
    }
}
