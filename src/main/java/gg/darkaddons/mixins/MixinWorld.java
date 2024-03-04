package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class, priority = 1_001)
final class MixinWorld {
    private MixinWorld() {
        super();
    }

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private final void doCheckLightFor$darkaddons(@SuppressWarnings("BoundedWildcard") @NotNull final CallbackInfoReturnable<Boolean> cir) {
        if (DarkAddons.isFullBright()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = {"getLightFromNeighborsFor", "getLightFromNeighbors", "getRawLight", "getLight(Lnet/minecraft/util/BlockPos;)I", "getLight(Lnet/minecraft/util/BlockPos;Z)I"}, at = @At("HEAD"), cancellable = true)
    private final void getLight$darkaddons(@SuppressWarnings("BoundedWildcard") @NotNull final CallbackInfoReturnable<Integer> cir) {
        if (DarkAddons.isFullBright()) {
            cir.setReturnValue(DarkAddons.MAXIMUM_LIGHT_LEVEL);
        }
    }
}
