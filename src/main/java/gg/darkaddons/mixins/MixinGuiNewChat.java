package gg.darkaddons.mixins;

import gg.darkaddons.mixin.MixinUtils;

import net.minecraft.client.gui.GuiNewChat;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiNewChat.class, priority = 1_001)
final class MixinGuiNewChat {
    private MixinGuiNewChat() {
        super();
    }

    @Inject(method = "addToSentMessages", at = @At("RETURN"))
    private final void afterAddToSentMessages$darkaddons(@NotNull final String message, @NotNull final CallbackInfo ci) {
        final var sentMessages = this.getSentMessages();

        while (100 < sentMessages.size()) {
            sentMessages.remove(0);
        }
    }

    @Shadow
    private final List<String> getSentMessages() {
        throw MixinUtils.shadowFail();
    }
}
