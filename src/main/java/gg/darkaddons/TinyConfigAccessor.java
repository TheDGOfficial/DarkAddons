package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

public final class TinyConfigAccessor {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private TinyConfigAccessor() {
        super();

        throw new UnsupportedOperationException();
    }

    public static final boolean getBoolean(@NotNull final String key, final boolean defaultValue) {
        return TinyConfig.getBoolean(key, defaultValue);
    }
}
