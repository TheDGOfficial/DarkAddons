package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import org.jetbrains.annotations.NotNull;

final class HideSigns {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private HideSigns() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean handleRenderTileEntity(@NotNull final TileEntity tileEntity) {
        McProfilerHelper.startSection("darkaddons_hide_signs");
        final var mc = Minecraft.getMinecraft();
        if (Config.isHideSigns() && null == mc.currentScreen && tileEntity instanceof TileEntitySign && (DarkAddons.isInDungeons() || DarkAddons.isPlayerInCrystalHollows())) {
            mc.theWorld.removeTileEntity(tileEntity.getPos());
            McProfilerHelper.endSection();
            return false;
        }
        McProfilerHelper.endSection();
        return true;
    }
}
