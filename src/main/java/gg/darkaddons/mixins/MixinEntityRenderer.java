package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.PublicUtils;
import gg.darkaddons.mixin.MixinUtils;
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

import java.util.function.Consumer;

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
                this.writeWhiteTexture$darkaddons();
            }

            this.updated = true;
            MixinEntityRenderer.sanityOp$darkaddons(this.lightmapTexture, DynamicTexture::updateDynamicTexture);

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

    private final void writeWhiteTexture$darkaddons() {
        final var texture = MixinEntityRenderer.sanityCheck$darkaddons(this.lightmapTexture);

        for (var i = 0; 256 > i; ++i) {
            texture.getTextureData()[i] = -1;
        }
    }

    @NotNull
    private static final DynamicTexture sanityCheck$darkaddons(@Nullable final DynamicTexture texture) {
        if (null == texture) {
            throw MixinUtils.shadowFail();
        }

        return texture;
    }

    private static final void sanityOp$darkaddons(@Nullable final DynamicTexture texture, @SuppressWarnings("BoundedWildcard") @NotNull final Consumer<DynamicTexture> op) {
        op.accept(MixinEntityRenderer.sanityCheck$darkaddons(texture));
    }

    @Override
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
