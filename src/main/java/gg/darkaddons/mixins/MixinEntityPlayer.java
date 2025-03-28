package gg.darkaddons.mixins;

import gg.darkaddons.annotations.bytecode.Name;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPlayer.class, priority = 1_001)
final class MixinEntityPlayer {
    private MixinEntityPlayer() {
        super();
    }

    @Shadow
    @Nullable
    private InventoryPlayer inventory;

    @Inject(method = "setCurrentItemOrArmor", at = @At("HEAD"), cancellable = true)
    private final void setCurrentItemOrArmor$darkaddons(final int slot, @NotNull final ItemStack itemStack, @NotNull final CallbackInfo ci) {
        final var currentItem = 0 == slot;

        final var array = currentItem ? this.inventory.mainInventory : this.inventory.armorInventory;
        final var index = currentItem ? this.inventory.currentItem : slot - 1;

        if (!MixinEntityPlayer.isArrayAccessInBounds$darkaddons(array, index)) {
            ci.cancel();
        }
    }

    @Unique
    private static final boolean isArrayAccessInBounds$darkaddons(@NotNull final ItemStack[] array, final int index) {
        return 0 <= index && index < array.length;
    }

    @Override
    @Unique
    @Name("toString$darkaddons")
    public final String toString() {
        //noinspection ObjectToString
        return "MixinEntityPlayer{" +
            "inventory=" + this.inventory +
            '}';
    }
}
