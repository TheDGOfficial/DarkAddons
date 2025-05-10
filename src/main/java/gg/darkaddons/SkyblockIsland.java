package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

enum SkyblockIsland {
    CrimsonIsle("crimson_isle"),
    TheRift("rift"),
    TheGarden("garden"),
    DungeonHub("dungeon_hub"),
    CrystalHollows("crystal_hollows"),
    TheEnd("combat_3"),
    ThePark("foraging_1"),
    SpiderDen("combat_1"),
    Hub("hub"),
    GlaciteMineshafts("mineshaft"),
    Dungeon("dungeon");

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
