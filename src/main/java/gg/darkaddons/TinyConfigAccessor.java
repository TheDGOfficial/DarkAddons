package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

public final class TinyConfigAccessor {
    public static final boolean getTinyConfigBoolean(@NotNull final String key, final boolean defaultValue) {
        return TinyConfig.getBoolean(key, defaultValue);
    }
}
