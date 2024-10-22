package gg.darkaddons.mixins;

import gg.darkaddons.mixin.MixinUtils;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(value = ItemStack.class, priority = 1_001)
final class MixinItemStack {
    private MixinItemStack() {
        super();
    }

    @Shadow
    private final Item getItem() {
        throw MixinUtils.shadowFail();
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"))
    private final void getDisplayName$darkaddons(@NotNull final CallbackInfoReturnable<String> cir) {
        if (null == this.getItem()) {
            cir.setReturnValue("");
        }
    }
}
