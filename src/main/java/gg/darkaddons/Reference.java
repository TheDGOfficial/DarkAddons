package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

final class Reference {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private Reference() {
        super();

        throw Utils.staticClassException();
    }

    @NotNull
    static final String MOD_ID = "darkaddons";

    @NotNull
    static final String VERSION = "@VERSION@";
}
