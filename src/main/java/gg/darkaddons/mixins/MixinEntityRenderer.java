package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.PublicUtils;
import gg.darkaddons.annotations.bytecode.Name;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = EntityRenderer.class, priority = 1_001)
final class MixinEntityRenderer {
    private MixinEntityRenderer() {
        super();
    }

    @Shadow
    @Final
    @Nullable
    private DynamicTexture lightmapTexture;

    @Shadow
    private boolean lightmapUpdateNeeded;

    @Unique
    private boolean updated;

    @Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
    private final void updateLightmap$darkaddons(final float partialTicks, @NotNull final CallbackInfo ci) {
        PublicUtils.startProfilingSection("darkaddons_fullbright_update_light_map");

        if (DarkAddons.isFullBright()) {
            if (!this.updated) {
                this.updated = true;

                final var texture = this.lightmapTexture;

                for (var i = 0; 256 > i; ++i) {
                    texture.getTextureData()[i] = -1;
                }

                texture.updateDynamicTexture();
            }

            PublicUtils.endProfilingSection();
            ci.cancel();

            return;
        }

        if (this.updated) {
            this.lightmapUpdateNeeded = true;
        }

        this.updated = false;

        PublicUtils.endProfilingSection();
    }

    @Override
    @Unique
    @Name("toString$darkaddons")
    public final String toString() {
        //noinspection ObjectToString
        return "MixinEntityRenderer{" +
            "lightmapTexture=" + this.lightmapTexture +
            ", lightmapUpdateNeeded=" + this.lightmapUpdateNeeded +
            ", updated=" + this.updated +
            '}';
    }
}
