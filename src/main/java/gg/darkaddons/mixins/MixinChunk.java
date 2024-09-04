package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Chunk.class, priority = 1_001)
final class MixinChunk {
    private MixinChunk() {
        super();
    }

    @Inject(method = {"getLightFor", "getLightSubtracted"}, at = @At("HEAD"), cancellable = true)
    private final void getLightLevel$darkaddons(@SuppressWarnings("BoundedWildcard") @NotNull final CallbackInfoReturnable<Integer> cir) {
        if (DarkAddons.isFullBright()) {
            cir.setReturnValue(DarkAddons.MAXIMUM_LIGHT_LEVEL);
        }
    }
}
