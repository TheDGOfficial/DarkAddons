package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.client.event.RenderWorldLastEvent;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;

import gg.darkaddons.DarkAddons;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.dungeons.solvers.IceFillSolver", priority = 999)
final class MixinIceFillSolver {
    private MixinIceFillSolver() {
        super();
    }

    @Inject(method = "onWorldRender", remap = false, at = @At("HEAD"), cancellable = true)
    private final void onWorldRender$darkaddons(@NotNull final RenderWorldLastEvent event, @NotNull final CallbackInfo ci) {
        if (-1L != DungeonTimer.INSTANCE.getBossEntryTime() || !DarkAddons.isInDungeons()) {
            ci.cancel();
        }
    }
}
