package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;
import gg.darkaddons.mixin.MixinUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.io.PrintStream;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import net.minecraft.world.World;

@Pseudo
@Mixin(targets = "me.Danker.features.puzzlesolvers.SilverfishSolver", remap = false, priority = 1_001)
final class MixinDSMSilverfishSolver {
    private MixinDSMSilverfishSolver() {
        super();
    }

    @Unique
    private static final ExecutorService solverTaskExecutor = Executors.newSingleThreadExecutor((@NotNull final Runnable r) -> PublicUtils.newThread(r, "Danker's Skyblock Mod Silverfish Solver Thread"));

    @Inject(method = "onTick", remap = false, cancellable = true, at = @At(value = "NEW", target = "java/lang/Thread", remap = false))
    private final void onTickBeforeNewThread$darkaddons(@NotNull final CallbackInfo ci) {
        // stop creating a thread every second, use a single thread executor instead that's parked when not in use.
        MixinDSMSilverfishSolver.executeSolverTaskAsync$darkaddons();
        ci.cancel();
    }

    @Redirect(method = "onTick", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V", remap = false))
    private final void onTickThreadStart$darkaddons(@NotNull final Thread thread) {
        // do nothing
    }

    @Redirect(method = "lambda$onTick$0", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/Arrays;deepToString([Ljava/lang/Object;)Ljava/lang/String;", remap = false))
    private static final String onTickArraysDeepToString$darkaddons(@NotNull final Object... board) {
        return ""; // Avoid toString computation since it will not be printed anymore.
    }

    @Redirect(method = "lambda$onTick$0", remap = false, at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V", remap = false))
    private static final void onTickPrintln$darkaddons(@NotNull final PrintStream out, @NotNull final String text) {
        // do nothing
    }

    @Shadow
    private static final void lambda$onTick$0(@NotNull final EntityPlayerSP player, @NotNull final World world) {
        throw MixinUtils.shadowFail();
    }

    @Unique
    private static final void executeSolverTaskAsync$darkaddons() {
        final var mc = Minecraft.getMinecraft();

        final var player = mc.thePlayer;
        final World world = mc.theWorld; // NOTE: Do not make it WorldClient (or var which makes it WorldClient implicitly), causes ProGuard to specialize method argument World in lambda$onTick$0 into WorldClient which causes shadowing to fail due to different method signature.

        if (null == player || null == world) {
            return;
        }

        MixinDSMSilverfishSolver.solverTaskExecutor.execute(() -> MixinDSMSilverfishSolver.lambda$onTick$0(player, world));
    }
}
