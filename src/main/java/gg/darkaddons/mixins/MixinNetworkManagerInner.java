package gg.darkaddons.mixins;

import gg.darkaddons.OptimizeLatency;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.network.NetworkManager$5", "net.minecraft.network.NetworkManager$6", "net.minecraft.client.network.OldServerPinger$2"}, priority = 1_001)
final class MixinNetworkManagerInner {
    private MixinNetworkManagerInner() {
        super();
    }

    @Inject(method = "initChannel", remap = false, at = @At("HEAD"))
    private final void initChannel$darkaddons(@NotNull final Channel channel, @NotNull final CallbackInfo ci) {
        OptimizeLatency.configureChannel(channel);
    }
}
