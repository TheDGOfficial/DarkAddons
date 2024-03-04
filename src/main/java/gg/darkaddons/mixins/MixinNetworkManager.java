package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.mixin.MixinUtils;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkManager.class, priority = 1_002)
final class MixinNetworkManager {
    private MixinNetworkManager() {
        super();
    }

    @Shadow(remap = false)
    @NotNull
    private final EnumPacketDirection getDirection() {
        throw MixinUtils.shadowFail();
    }

    @Shadow
    private final boolean isLocalChannel() {
        throw MixinUtils.shadowFail();
    }

    @Inject(method = "channelRead0", at = @At("HEAD"))
    private final void onReceivePacket$darkaddons(@NotNull final ChannelHandlerContext context, @NotNull final Packet<?> packet, @NotNull final CallbackInfo ci) {
        if (EnumPacketDirection.CLIENTBOUND == this.getDirection() && !this.isLocalChannel()) {
            DarkAddons.onClientPacketReceive(packet);
        }
    }
}
