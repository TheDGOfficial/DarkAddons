package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.PublicUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.Skytils", remap = false, priority = 1_001)
final class MixinSkytils {
    private MixinSkytils() {
        super();
    }

    @Redirect(method = "<clinit>", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Executors;newFixedThreadPool(I)Ljava/util/concurrent/ExecutorService;", remap = false))
    private static final ExecutorService newFixedThreadPool$darkaddons(final int originalPoolSize) {
        return Executors.newFixedThreadPool(DarkAddons.isReduceBackgroundThreads() ? Math.min(10, Runtime.getRuntime().availableProcessors() << 1) : 10, (@NotNull final Runnable r) -> PublicUtils.newThread(r, "Skytils Thread"));
    }
}
