package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndPortal;
import org.jetbrains.annotations.NotNull;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;

final class HideEndPortals {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private HideEndPortals() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean handleRenderTileEntity(@NotNull final TileEntity tileEntity) {
        McProfilerHelper.startSection("darkaddons_hide_end_portals");
        final var mc = Minecraft.getMinecraft();
        final var player = mc.thePlayer;
        if (Config.isHideEndPortals() && tileEntity instanceof TileEntityEndPortal && (-1L != DungeonTimer.INSTANCE.getPhase4ClearTime() || (null != player && player.getPosition().getY() <= 45)) && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
            McProfilerHelper.endSection();
            return false;
        }
        McProfilerHelper.endSection();
        return true;
    }
}