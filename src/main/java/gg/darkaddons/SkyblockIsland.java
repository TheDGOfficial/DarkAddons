package gg.darkaddons;

import gg.skytils.skytilsmod.utils.SBInfo;

import org.jetbrains.annotations.NotNull;

enum SkyblockIsland {
    CrimsonIsle(gg.skytils.skytilsmod.utils.SkyblockIsland.CrimsonIsle),
    TheRift(gg.skytils.skytilsmod.utils.SkyblockIsland.TheRift),
    TheGarden(gg.skytils.skytilsmod.utils.SkyblockIsland.TheGarden),
    DungeonHub(gg.skytils.skytilsmod.utils.SkyblockIsland.DungeonHub),
    CrystalHollows(gg.skytils.skytilsmod.utils.SkyblockIsland.CrystalHollows),
    TheEnd(gg.skytils.skytilsmod.utils.SkyblockIsland.TheEnd),
    ThePark(gg.skytils.skytilsmod.utils.SkyblockIsland.ThePark),
    SpiderDen(gg.skytils.skytilsmod.utils.SkyblockIsland.SpiderDen),
    Hub(gg.skytils.skytilsmod.utils.SkyblockIsland.Hub),
    GlaciteMineshafts(gg.skytils.skytilsmod.utils.SkyblockIsland.GlaciteMineshafts),
    Dungeon(gg.skytils.skytilsmod.utils.SkyblockIsland.Dungeon);

    @NotNull
    private final gg.skytils.skytilsmod.utils.SkyblockIsland island;

    private SkyblockIsland(@NotNull final gg.skytils.skytilsmod.utils.SkyblockIsland island) {
        this.island = island;
    }

    final boolean isInIsland() {
        return this.island.getMode().equals(SBInfo.INSTANCE.getMode());
    }
}
