package gg.darkaddons.mixins;

import gg.darkaddons.OptimizeLatency;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChannelInitializer.class, priority = 1_001)
final class MixinChannelInitializer {
    private MixinChannelInitializer() {
        super();
    }

    @Inject(method = "channelRegistered", remap = false, at = @At("HEAD"))
    private final void channelRegistered$darkaddons(@NotNull final ChannelHandlerContext channelHandlerContext, @NotNull final CallbackInfo ci) {
        OptimizeLatency.configureChannel(channelHandlerContext.channel());
    }
}
