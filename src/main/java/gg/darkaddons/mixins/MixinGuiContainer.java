package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.jetbrains.annotations.NotNull;

@Mixin(value = GuiContainer.class, priority = 1_001)
final class MixinGuiContainer {
    private MixinGuiContainer() {
        super();
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private final void drawSlot$darkaddons(@NotNull final Slot slot, @NotNull final CallbackInfo ci) {
        DarkAddons.handleDrawSlot((GuiContainer) (Object) this, slot);
    }
}
