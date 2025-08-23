package gg.darkaddons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;

final class RemoveBlankArmorStands {
    RemoveBlankArmorStands() {
        super();
    }

    private static final boolean isInventoryEmpty(@NotNull final Entity entity) {
        for (final var item : entity.getInventory()) {
            //noinspection VariableNotUsedInsideIf
            if (null != item) {
                return false;
            }
        }
        return true;
    }

    static final boolean checkRender(@NotNull final EntityArmorStand entity) {
        return !Config.isRemoveBlankArmorStands() || 10 <= entity.ticksExisted || !entity.getCustomNameTag().isEmpty() || !RemoveBlankArmorStands.isInventoryEmpty(entity);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onEntityJoinWorld(@NotNull final EntityJoinWorldEvent event) {
        final var entity = event.entity;

        if (Config.isRemoveBlankArmorStands() && entity instanceof EntityArmorStand && entity.isInvisible()) {
            final var inventory = entity.getInventory();

            ItemStack singleItem = null;
            var nonNullCount = 0;

            for (final var item : inventory) {
                if (null != item) {
                    ++nonNullCount;
                    if (1 < nonNullCount) {
                        return;
                    }
                    singleItem = item;
                }
            }

            if (1 == nonNullCount && singleItem.getItem() instanceof ItemBlock) {
                entity.setDead();
            }
        }
    }
}
