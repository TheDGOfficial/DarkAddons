package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;

final class ItemUtils {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private ItemUtils() {
        super();

        throw Utils.staticClassException();
    }

    @Nullable
    static final ItemStack getHeldItemStack(@NotNull final Minecraft mc) {
        return mc.thePlayer.getHeldItem();
    }

    @Nullable
    static final Item getHeldItem(@NotNull final Minecraft mc) {
        return ItemUtils.getItem(ItemUtils.getHeldItemStack(mc));
    }

    @Nullable
    private static final Item getItem(@Nullable final ItemStack itemStack) {
        return null == itemStack ? null : itemStack.getItem();
    }

    static final boolean isHoldingItemContaining(@NotNull final Minecraft mc, @SuppressWarnings("TypeMayBeWeakened") @NotNull final String search) {
        final var itemStack = ItemUtils.getHeldItemStack(mc);

        return null != itemStack && itemStack.getDisplayName().contains(search);
    }

    @NotNull
    static final List<String> getItemLore(@NotNull final ItemStack itemStack) {
        final var tagCompound = itemStack.getTagCompound();
        if (null != tagCompound) {
            final var display = tagCompound.getCompoundTag("display");
            if (null != display) {
                final var tagList = display.getTagList("Lore", 8);
                if (null != tagList) {
                    final var length = tagList.tagCount();

                    final var lore = new ArrayList<String>(length);
                    for (var i = 0; i < length; ++i) {
                        lore.add(tagList.getStringTagAt(i));
                    }

                    return Collections.unmodifiableList(lore);
                }
            }
        }
        return Collections.emptyList();
    }
}
