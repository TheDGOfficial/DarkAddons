package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.mixin.MixinUtils;

import java.util.concurrent.Future;

import io.netty.channel.EventLoop;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;

import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Inject(method = "channelRead0", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V"), cancellable = true)
    private final void onReceivePacket$darkaddons(@NotNull final ChannelHandlerContext context, @NotNull final Packet<?> packet, @NotNull final CallbackInfo ci) {
        if (EnumPacketDirection.CLIENTBOUND == this.getDirection() && !this.isLocalChannel() && !DarkAddons.onClientPacketReceive(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "dispatchPacket", at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;", remap = false))
    private final void onSendPacket$darkaddons(@NotNull final Packet<?> packet, @Nullable final GenericFutureListener<? extends Future<? super Void>>[] futureListeners, @NotNull final CallbackInfo ci) {
        if (EnumPacketDirection.CLIENTBOUND == this.getDirection() && !this.isLocalChannel()) {
            DarkAddons.onPacketSent(packet);
        }
    }

    @Redirect(method = "dispatchPacket", at = @At(value = "INVOKE", target = "Lio/netty/channel/EventLoop;execute(Ljava/lang/Runnable;)V", remap = false))
    private final void onExecutePacketSend$darkaddons(@NotNull final EventLoop eventLoop, @NotNull final Runnable originalRunnable, @NotNull final Packet<?> packet, @NotNull final GenericFutureListener<? extends Future<? super Void>>[] futureListeners) {
        eventLoop.execute(() -> {
            DarkAddons.onPacketSent(packet);
            originalRunnable.run();
        });
    }
}
