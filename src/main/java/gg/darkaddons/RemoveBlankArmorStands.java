package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;

final class RemoveBlankArmorStands {
    static final int BLANK_ARMOR_STAND_REMOVAL_INTERVAL_IN_TICKS = 100;

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

    private static final boolean isAnyGuardiansTargetingEntity(@NotNull final WorldClient world, @NotNull final Entity entity) {
        for (final var ent : world.loadedEntityList) {
            if (ent instanceof final EntityGuardian guardian) {
                if (entity == guardian.getTargetedEntity()) {
                    return true;
                }
            }
        }
        return false;
    }

    static final boolean removeIfBlankArmorStand(@NotNull final WorldClient world, @NotNull final Entity entity) {
        //noinspection ObjectEquality
        if ((!AdditionalM7Features.isInM7OrF7() || AdditionalM7Features.phase5Started) && AdditionalM7Features.canRemoveBlankArmorStands() && 0.0D == entity.motionX && 0.0D == entity.motionY && 0.0D == entity.motionZ && 10 < entity.ticksExisted && Minecraft.getMinecraft().thePlayer.ridingEntity != entity && !RemoveBlankArmorStands.isAnyGuardiansTargetingEntity(world, entity)) {
            final var nameTag = entity.getCustomNameTag();
            NameTagCache.setLastNameTag(nameTag);

            if (nameTag.isEmpty()) {
                if (RemoveBlankArmorStands.isInventoryEmpty(entity)) {
                    world.removeEntityFromWorld(entity.getEntityId());
                    return true;
                }
            }
        }
        return false;
    }

    static final boolean checkRender(@NotNull final Entity entity) {
        return !Config.isRemoveBlankArmorStands() || 10 <= entity.ticksExisted || !entity.getCustomNameTag().isEmpty() || !RemoveBlankArmorStands.isInventoryEmpty(entity);
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
