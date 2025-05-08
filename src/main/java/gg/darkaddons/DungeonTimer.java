package gg.darkaddons;

public final class DungeonTimer {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private DungeonTimer() {
        super();

        throw Utils.staticClassException();
    }

    static final long getDungeonStartTime() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer.INSTANCE.getDungeonStartTime();
    }

    public static final long getBossEntryTime() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer.INSTANCE.getBossEntryTime();
    }

    public static final long getBossClearTime() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer.INSTANCE.getBossClearTime();
    }

    public static final long getPhase1ClearTime() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer.INSTANCE.getPhase1ClearTime();
    }

    static final long getPhase2ClearTime() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer.INSTANCE.getPhase2ClearTime();
    }

    static final long getPhase3ClearTime() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer.INSTANCE.getPhase3ClearTime();
    }

    static final long getPhase4ClearTime() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer.INSTANCE.getPhase4ClearTime();
    }

    static final long getTerraClearTime() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer.INSTANCE.getTerraClearTime();
    }
}
