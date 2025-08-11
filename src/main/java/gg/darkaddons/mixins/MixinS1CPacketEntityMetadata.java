package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;

import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import org.jetbrains.annotations.NotNull;

@Mixin(value = S1CPacketEntityMetadata.class, priority = 999)
final class MixinS1CPacketEntityMetadata {
    private MixinS1CPacketEntityMetadata() {
        super();
    }

    @Redirect(method = "processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/INetHandlerPlayClient;handleEntityMetadata(Lnet/minecraft/network/play/server/S1CPacketEntityMetadata;)V"), require = 0)
    private final void redirectProcessPacket(@NotNull final INetHandlerPlayClient instance, @NotNull final S1CPacketEntityMetadata packet) {
        instance.handleEntityMetadata(packet);

        DarkAddons.onEntityMetadata(packet);
    }
}
