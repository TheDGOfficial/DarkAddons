package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.mixin.MixinUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Pseudo
@Mixin(targets = "gg.essential.util.Multithreading", remap = false, priority = 1_001)
final class MixinMultithreadingOther {
    private MixinMultithreadingOther() {
        super();
    }

    @Shadow(remap = false)
    @Final
    @Nullable
    private static ThreadPoolExecutor POOL;

    @Inject(method = "<clinit>", remap = false, at = @At("TAIL"))
    private static final void afterStaticInitializer$darkaddons(@NotNull final CallbackInfo ci) {
        if (null == MixinMultithreadingOther.POOL) {
            throw MixinUtils.shadowFail();
        }

        if (DarkAddons.isReduceBackgroundThreads()) {
            final var cpuTotalThreadsAmount = Runtime.getRuntime().availableProcessors();

            MixinMultithreadingOther.POOL.setCorePoolSize(Math.min(10, cpuTotalThreadsAmount));
            MixinMultithreadingOther.POOL.setMaximumPoolSize(Math.min(30, cpuTotalThreadsAmount << 1));

            final var oldFactory = MixinMultithreadingOther.POOL.getThreadFactory();
            MixinMultithreadingOther.POOL.setThreadFactory((@NotNull final Runnable r) -> {
                final var thread = oldFactory.newThread(r);

                final var oldName = thread.getName();
                if (!oldName.contains("Essential")) {
                    thread.setName(StringUtils.replace(oldName, "Thread ", "Essential Thread "));
                }

                return thread;
            });
        }
    }

    @Redirect(method = "<clinit>", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Executors;newScheduledThreadPool(ILjava/util/concurrent/ThreadFactory;)Ljava/util/concurrent/ScheduledExecutorService;", remap = false))
    private static final ScheduledExecutorService newScheduledThreadPool$darkaddons(final int originalSize, @NotNull final ThreadFactory threadFactory) {
        return Executors.newScheduledThreadPool(DarkAddons.isReduceBackgroundThreads() ? Math.min(10, Runtime.getRuntime().availableProcessors() << 1) : 10, threadFactory);
    }
}
