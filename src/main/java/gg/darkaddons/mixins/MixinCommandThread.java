package gg.darkaddons.mixins;

import paulscode.sound.CommandThread;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandThread.class, remap = false, priority = 1_001)
final class MixinCommandThread {
    private MixinCommandThread() {
        super();
    }

    @Inject(method = "run", remap = false, at = @At("HEAD"))
    private final void run$darkaddons(@NotNull final CallbackInfo ci) {
        Thread.currentThread().setName("Sound Thread");
    }
}
