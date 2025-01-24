package gg.darkaddons.mixins;

import gg.darkaddons.OptimizeLatency;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.DefaultChannelConfig;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultChannelConfig.class, priority = 1_001)
final class MixinDefaultChannelConfig {
    private MixinDefaultChannelConfig() {
        super();
    }

    @Inject(method = "<init>", remap = false, at = @At("RETURN"))
    private final void init$darkaddons(@NotNull final Channel channel, @NotNull final CallbackInfo ci) {
        OptimizeLatency.configureChannelConfig((ChannelConfig) (Object) this, () -> true); // Skip socket-specific options, those should be set from MixinDefaultSocketChannelConfig, because this mixin's init method is called for DefaultSocketChannelConfig's as well, and at that point in time, the javaSocket is null, it causes NPE otherwise.
    }
}
