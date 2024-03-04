package gg.darkaddons.mixins;

import gg.darkaddons.OptimizeLatency;
import io.netty.channel.ChannelConfig;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.DefaultSocketChannelConfig;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Socket;

@Mixin(value = DefaultSocketChannelConfig.class, priority = 1_001)
final class MixinDefaultSocketChannelConfig {
    private MixinDefaultSocketChannelConfig() {
        super();
    }

    @Inject(method = "<init>", remap = false, at = @At("TAIL"))
    private final void init$darkaddons(@NotNull final SocketChannel channel, @NotNull final Socket javaSocket, @NotNull final CallbackInfo ci) {
        OptimizeLatency.configureChannelConfig((ChannelConfig) (Object) this, () -> false);
    }
}
