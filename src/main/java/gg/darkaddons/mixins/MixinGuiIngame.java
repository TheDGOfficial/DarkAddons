package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.client.gui.GuiIngame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngame.class, priority = 1_001)
final class MixinGuiIngame {
    private MixinGuiIngame() {
        super();
    }

    @Inject(method = "displayTitle", at = @At("HEAD"))
    private final void displayTitle$darkaddons(@Nullable final String title, @Nullable final String subTitle, final int timeFadeIn, final int displayTime, final int timeFadeOut, @NotNull final CallbackInfo ci) {
        DarkAddons.handleSubTitleUpdate(subTitle);
    }
}
