package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

enum SkyblockIsland {
    CrimsonIsle("crimson_isle"),
    TheRift("rift"),
    DungeonHub("dungeon_hub"),
    CrystalHollows("crystal_hollows"),
    TheEnd("combat_3"),
    ThePark("foraging_1"),
    SpiderDen("combat_1"),
    Hub("hub"),
    Dungeon("dungeon"),
    TheGarden("garden"),
    PrivateIsland("dynamic");

    @NotNull
    private final String mode;

    private SkyblockIsland(@NotNull final String mode) {
        this.mode = mode;
    }

    final boolean isInIsland() {
        return this.mode.equals(SBInfo.getMode());
    }

    @Override
    public final String toString() {
        return "SkyblockIsland{" +
            "mode='" + this.mode + '\'' +
            '}';
    }
}
