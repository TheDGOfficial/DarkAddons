package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import java.util.ArrayList;

final class RemoveBlankArmorStands {
    static final int BLANK_ARMOR_STAND_REMOVAL_INTERVAL_IN_TICKS = 100;

    RemoveBlankArmorStands() {
        super();
    }

    static final boolean removeIfBlankArmorStand(@NotNull final WorldClient world, @NotNull final Entity entity) {
        //noinspection ObjectEquality
        if ((!AdditionalM7Features.isInM7OrF7() || AdditionalM7Features.phase5Started) && AdditionalM7Features.canRemoveBlankArmorStands() && 0.0D == entity.motionX && 0.0D == entity.motionY && 0.0D == entity.motionZ && Minecraft.getMinecraft().thePlayer.ridingEntity != entity) {
            final var nameTag = entity.getCustomNameTag();
            NameTagCache.setLastNameTag(nameTag);

            if (nameTag.isEmpty()) {
                var hasNoItems = true;
                for (final var item : entity.getInventory()) {
                    //noinspection VariableNotUsedInsideIf
                    if (null != item) {
                        hasNoItems = false;
                        break;
                    }
                }
                if (hasNoItems) {
                    world.removeEntityFromWorld(entity.getEntityId());
                    return true;
                }
            }
        }
        return false;
    }

    private static final void removeBlankArmorStands() {
        if (Config.isRemoveBlankArmorStands() && !Config.isArmorStandOptimizer()) {
            final var mc = Minecraft.getMinecraft();
            final var world = mc.theWorld;

            if (null != world) {
                for (final var entity : world.loadedEntityList) {
                    if (entity instanceof EntityArmorStand) {
                        RemoveBlankArmorStands.removeIfBlankArmorStand(world, entity);
                        NameTagCache.clearLastNameTag();
                    }
                }
            }
        }
    }

    static final void registerPeriodicRemoval() {
        DarkAddons.registerTickTask("remove_blank_armor_stands", RemoveBlankArmorStands.BLANK_ARMOR_STAND_REMOVAL_INTERVAL_IN_TICKS, true, RemoveBlankArmorStands::removeBlankArmorStands);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onEntityJoinWorld(@NotNull final EntityJoinWorldEvent event) {
        final var entity = event.entity;

        if (Config.isRemoveBlankArmorStands() && entity instanceof EntityArmorStand && entity.isInvisible()) {
            final var inventory = entity.getInventory();
            final var list = new ArrayList<ItemStack>(inventory.length);
            for (final var item : inventory) {
                if (null != item) {
                    list.add(item);
                }
            }
            if (1 == list.size() && list.get(0).getItem() instanceof ItemBlock) {
                entity.setDead();
            }
        }
    }
}
