package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

final class MayorInfo {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private MayorInfo() {
        super();

        throw Utils.staticClassException();
    }

    @Nullable
    static final String getCurrentMayor() {
        return gg.skytils.skytilsmod.features.impl.handlers.MayorInfo.INSTANCE.getCurrentMayor();
    }

    @NotNull
    static final HashSet<String> getAllPerks() {
        return gg.skytils.skytilsmod.features.impl.handlers.MayorInfo.INSTANCE.getAllPerks();
    }

    @Nullable
    static final String getJerryMayor() {
        final var jerryMayor = gg.skytils.skytilsmod.features.impl.handlers.MayorInfo.INSTANCE.getJerryMayor();

        return null == jerryMayor ? null : jerryMayor.getName();
    }
}
