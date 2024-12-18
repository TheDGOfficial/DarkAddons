package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import org.jetbrains.annotations.NotNull;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;

final class HideSkulls {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private HideSkulls() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean handleRenderTileEntity(@NotNull final TileEntity tileEntity) {
        McProfilerHelper.startSection("darkaddons_hide_skulls");
        final var mc = Minecraft.getMinecraft();
        final var player = mc.thePlayer;
        if (Config.isHideSkulls() && tileEntity instanceof TileEntitySkull && AdditionalM7Features.phase5Started && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
            McProfilerHelper.endSection();
            return false;
        }
        McProfilerHelper.endSection();
        return true;
    }
}
