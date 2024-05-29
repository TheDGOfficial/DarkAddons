package gg.darkaddons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import gg.darkaddons.DarkAddons;

@Mixin(value = ChunkRenderDispatcher.class, priority = 999)
final class MixinChunkRenderDispatcher {
    private MixinChunkRenderDispatcher() {
        super();
    }

    @Inject(method = "getNextChunkUpdate", at = @At("HEAD"))
    private final void getNextChunkUpdate$darkaddons(@NotNull final CallbackInfoReturnable<ChunkCompileTaskGenerator> cir) throws InterruptedException {
        if (DarkAddons.isDelayChunkUpdates() && DarkAddons.isInitialized()) {
            Thread.sleep(Math.min(500L, DarkAddons.getLastGameLoopTime()));
        }
    }
}
