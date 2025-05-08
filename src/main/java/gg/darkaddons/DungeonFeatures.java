package gg.darkaddons;

import org.jetbrains.annotations.Nullable;

public final class DungeonFeatures {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private DungeonFeatures() {
        super();

        throw Utils.staticClassException();
    }

    @Nullable
    static final Integer getDungeonFloorNumber() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.INSTANCE.getDungeonFloorNumber();
    }

    static final boolean getHasBossSpawned() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.INSTANCE.getHasBossSpawned();
    }

    @Nullable
    static final String getDungeonFloor() {
        return gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.INSTANCE.getDungeonFloor();
    }
}
