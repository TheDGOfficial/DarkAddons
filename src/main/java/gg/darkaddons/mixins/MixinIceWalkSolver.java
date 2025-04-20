package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.client.event.RenderWorldLastEvent;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;

import gg.darkaddons.DarkAddons;

@Pseudo
@Mixin(targets = "me.Danker.features.puzzlesolvers.IceWalkSolver", priority = 999)
final class MixinIceWalkSolver {
    private MixinIceWalkSolver() {
        super();
    }

    @Redirect(method = "onTick", remap = false, at = @At(value = "NEW", target = "java/lang/Thread"))
    private final Thread newThread$darkaddons(@NotNull final Runnable task) {
        final var thread = new Thread(task, "Danker's Skyblock Mod Ice Walk Solver Thread");
        thread.setPriority(Thread.MIN_PRIORITY);

        return thread;
    }

    @Inject(method = "onWorldRender", remap = false, at = @At("HEAD"), cancellable = true)
    private final void onWorldRender$darkaddons(@NotNull final RenderWorldLastEvent event, @NotNull final CallbackInfo ci) {
        if (-1L != DungeonTimer.INSTANCE.getBossEntryTime() || !DarkAddons.isInDungeons()) {
            ci.cancel();
        }
    }
}
