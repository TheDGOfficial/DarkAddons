package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.PublicUtils;

import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.handlers.Waypoints", remap = false)
final class MixinWaypoints {
    private MixinWaypoints() {
        super();
    }

    @Inject(method = "onWorldRender", at = @At("HEAD"), remap = false, cancellable = true)
    private final void onWorldRenderPre$darkaddons(@NotNull final RenderWorldLastEvent event, @NotNull final CallbackInfo ci) {
        if (DarkAddons.shouldNotRenderSkytilsWaypoints()) {
            ci.cancel();
            return;
        }
        PublicUtils.startProfilingSection("skytils_render_waypoints");
    }

    @Inject(method = "onWorldRender", at = @At("RETURN"), remap = false)
    private final void onWorldRenderPost$darkaddons(@NotNull final RenderWorldLastEvent event, @NotNull final CallbackInfo ci) {
        PublicUtils.endProfilingSection();
    }
}
