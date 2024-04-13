package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MiningPingFix {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private MiningPingFix() {
        super();

        throw Utils.staticClassException();
    }

    private static final boolean isMiningItem(@NotNull final Item item) {
        // prismarine_shard = drills, skull = gemstone gauntlet, rest are vanilla pickaxes plus pickonimbus 2000 and titanium pickaxes along with some other SB pickaxes.
        return Items.prismarine_shard == item || Items.skull == item || Items.wooden_pickaxe == item || Items.stone_pickaxe == item || Items.golden_pickaxe == item || Items.iron_pickaxe == item || Items.diamond_pickaxe == item;
    }

    static final boolean isHittingPosition(@NotNull final BlockPos pos, @Nullable final ItemStack currentItemHittingBlock, @NotNull final BlockPos currentBlock) {
        final var posEq = pos.equals(currentBlock);

        final var itemstack = Minecraft.getMinecraft().thePlayer.getHeldItem();
        var flag = null == currentItemHittingBlock && null == itemstack;

        if (null != currentItemHittingBlock && null != itemstack) {
            final var itemstackItem = itemstack.getItem();
            final var currentItemHittingBlockItem = currentItemHittingBlock.getItem();

            if (Config.isMiningPingFix() && MiningPingFix.isMiningItem(itemstackItem)) {
                return posEq && itemstackItem == currentItemHittingBlockItem;
            }

            flag = itemstackItem == currentItemHittingBlockItem && ItemStack.areItemStackTagsEqual(itemstack, currentItemHittingBlock) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == currentItemHittingBlock.getMetadata());
        }

        return posEq && flag;
    }
}
