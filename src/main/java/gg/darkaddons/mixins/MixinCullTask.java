package gg.darkaddons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gg.darkaddons.DarkAddons;

@Pseudo
@Mixin(targets = "dev.tr7zw.entityculling.CullTask", remap = false, priority = 1_001)
final class MixinCullTask {
    private MixinCullTask() {
        super();
    }

    @Redirect(method = "run", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V", remap = false))
    private final void sleep$darkaddons(final long originalSleepTime) throws InterruptedException {
        Thread.sleep(Math.max(150L, originalSleepTime) + (DarkAddons.isInitialized() ? DarkAddons.getLastGameLoopTime() : 0L));
    }
}
