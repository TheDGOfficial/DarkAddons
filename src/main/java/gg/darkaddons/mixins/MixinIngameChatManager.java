package gg.darkaddons.mixins;

import gg.darkaddons.mixin.MixinUtils;

import net.minecraft.client.gui.GuiNewChat;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "net.labymod.ingamechat.IngameChatManager", remap = false, priority = 1_001)
final class MixinIngameChatManager {
    private MixinIngameChatManager() {
        super();
    }

    @Inject(method = "addToSentMessages", at = @At(value = "RETURN"))
    private final void afterAddToSentMessages$darkaddons(@NotNull final String message, @NotNull final CallbackInfo ci) {
        final var sentMessages = this.getSentMessages();

        while (sentMessages.size() > 100) {
            sentMessages.remove(0);
        }
    }

    @Shadow
    private final List<String> getSentMessages() {
        throw MixinUtils.shadowFail();
    }
}
