package gg.darkaddons;

final class SkyblockDedection {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private SkyblockDedection() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean isInSkyblock() {
        return gg.skytils.skytilsmod.utils.Utils.INSTANCE.getInSkyblock();
    }
}
