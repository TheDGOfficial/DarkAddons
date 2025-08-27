package gg.darkaddons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

@Pseudo
@Mixin(targets = "net.labymod.labyconnect.LabyConnect", remap = false, priority = 1_001)
final class MixinLabyConnect {
    private MixinLabyConnect() {
        super();
    }

    @Redirect(method = "<init>", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ScheduledExecutorService;scheduleWithFixedDelay(Ljava/lang/Runnable;JJLjava/util/concurrent/TimeUnit;)Ljava/util/concurrent/ScheduledFuture;", remap = false))
    private final ScheduledFuture<?> scheduleWithFixedDelay$darkaddons(@NotNull final ScheduledExecutorService executor, @NotNull final Runnable task, final long initialDelay, final long runInterval, @NotNull final TimeUnit timeUnit) {
        return null;
    }
}
