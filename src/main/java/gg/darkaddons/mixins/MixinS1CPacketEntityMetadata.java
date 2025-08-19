package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;

import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.NotNull;

@Mixin(value = S1CPacketEntityMetadata.class, priority = 1001)
final class MixinS1CPacketEntityMetadata {
    private MixinS1CPacketEntityMetadata() {
        super();
    }

    @Inject(method = "processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/INetHandlerPlayClient;handleEntityMetadata(Lnet/minecraft/network/play/server/S1CPacketEntityMetadata;)V", shift = At.Shift.AFTER))
    private final void afterProcessPacket(@NotNull final INetHandlerPlayClient instance, @NotNull final CallbackInfo ci) {
        DarkAddons.onEntityMetadata((S1CPacketEntityMetadata) (Object) this);
    }
}
