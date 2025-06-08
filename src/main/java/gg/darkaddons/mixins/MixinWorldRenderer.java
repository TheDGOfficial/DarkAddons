package gg.darkaddons.mixins;

import java.nio.ByteBuffer;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.WorldRenderer;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WorldRenderer.class, priority = 999)
final class MixinWorldRenderer {
    private MixinWorldRenderer() {
        super();
    }

    private static final int INCREASED_DEFAULT_BUFFER_BYTES = 2_621_440; // 655360 * 4

    @Redirect(method = "<init>(I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GLAllocation;createDirectByteBuffer(I)Ljava/nio/ByteBuffer;"))
    @NotNull
    private final ByteBuffer redirectCreateDirectByteBuffer$darkaddons(final int ignoredSize) {
        return GLAllocation.createDirectByteBuffer(MixinWorldRenderer.INCREASED_DEFAULT_BUFFER_BYTES);
    }
}

