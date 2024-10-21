package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.ItemStack;

@Pseudo
@Mixin(targets = "me.Danker.features.PetColours", remap = false, priority = 1_001)
final class MixinDSMPetColours {
    private MixinDSMPetColours() {
        super();
    }

    @Redirect(method = "onGuiRender", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getDisplayName()Ljava/lang/String;", remap = true))
    @NotNull
    private final String getDisplayName$darkaddons(@NotNull final ItemStack itemStack) {
        return null == itemStack.getItem() ? "" : itemStack.getDisplayName();
    }
}
