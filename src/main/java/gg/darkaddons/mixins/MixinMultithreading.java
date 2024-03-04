package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.mixin.MixinUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadPoolExecutor;

@Pseudo
@Mixin(targets = "gg.essential.api.utils.Multithreading", remap = false, priority = 1_001)
final class MixinMultithreading {
    private MixinMultithreading() {
        super();
    }

    @Shadow(remap = false)
    @Final
    @Nullable
    private static ThreadPoolExecutor POOL;

    @Inject(method = "<clinit>", remap = false, at = @At("TAIL"))
    private static final void afterStaticInitializer$darkaddons(@NotNull final CallbackInfo ci) {
        if (null == MixinMultithreading.POOL) {
            throw MixinUtils.shadowFail();
        }

        if (DarkAddons.isReduceBackgroundThreads()) {
            final var cpuTotalThreadsAmount = Runtime.getRuntime().availableProcessors();

            MixinMultithreading.POOL.setCorePoolSize(Math.min(10, cpuTotalThreadsAmount));
            MixinMultithreading.POOL.setMaximumPoolSize(Math.min(30, cpuTotalThreadsAmount << 1));
        }
    }
}
