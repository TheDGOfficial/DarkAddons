package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityRendererDispatcher.class, priority = 1_001)
final class MixinTileEntityRendererDispatcher {
    private MixinTileEntityRendererDispatcher() {
        super();
    }

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At("HEAD"))
    private final void renderTileEntityAt$darkaddons(@NotNull final TileEntity tileEntityIn, final double x, final double y, final double z, final float partialTicks, final int destroyStage, @NotNull final CallbackInfo ci) {
        DarkAddons.handleRenderTileEntity(tileEntityIn);
    }
}
