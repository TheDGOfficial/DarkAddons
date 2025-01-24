package gg.darkaddons.mixins;

import gg.darkaddons.mixin.MixinUtils;

import gg.darkaddons.DarkAddons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

@Pseudo
@Mixin(targets = "cc.polyfrost.oneconfig.internal.config.core.ConfigCore", remap = false, priority = 1_001)
final class MixinConfigCore {
    private MixinConfigCore() {
        super();
    }

    @Shadow
    private static final void saveAll() {
        throw MixinUtils.shadowFail();
    }

    @Inject(method = "<clinit>", remap = false, at = @At(value = "RETURN"))
    private static final void afterStaticInitializer$darkaddons(@NotNull final CallbackInfo ci) {
        // Save before cleanly exiting the game via "Quit Game" in the main menu in case periodic save didn't kick in after changing settings in time (or disabled)
        DarkAddons.addShutdownTask(MixinConfigCore::saveAll);
    }

    @Redirect(method = "<clinit>", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ScheduledExecutorService;scheduleAtFixedRate(Ljava/lang/Runnable;JJLjava/util/concurrent/TimeUnit;)Ljava/util/concurrent/ScheduledFuture;", remap = false), require = 0)
    @Nullable
    private static final ScheduledFuture<?> scheduleAtFixedRate$darkaddons(@NotNull final ScheduledExecutorService executor, @NotNull final Runnable task, final long initialDelay, final long runInterval, @NotNull final TimeUnit timeUnit) {
        // scheduleWithFixedDelay instead of scheduleAtFixedRate: this will take time took to save the files to the config into account, so if let's say saving the config took 29.9 seconds and the runInterval * 20 = 30 seconds, it will not re-save it after 0.1 seconds again but will wait 30 seconds.

        // runInterval * 20: one config saves all configs to disk every 30 seconds even if its unchanged, this makes it 600 seconds (10 minutes), this should reduce I/O and increase SSD lifespan/reduce wear.
        return DarkAddons.isDisablePeriodicConfigSaves() ? null : executor.scheduleWithFixedDelay(task, runInterval/* * 20L*/, runInterval/* * 20L*/, timeUnit);
    }
}
