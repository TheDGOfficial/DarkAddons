package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.NotNull;

final class RemoveBlankArmorStands {
    static final int BLANK_ARMOR_STAND_REMOVAL_INTERVAL_IN_TICKS = 100;

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     *
     * @implNote The thrown {@link UnsupportedOperationException} will have no
     * message to not waste a {@link String} instance in the constant pool.
     */
    private RemoveBlankArmorStands() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean removeIfBlankArmorStand(@NotNull final WorldClient world, @NotNull final Entity entity) {
        //noinspection ObjectEquality
        if ((!HideWitherSkeletons.isInF7OrM7() || AdditionalM7Features.phase5Started) && AdditionalM7Features.canRemoveBlankArmorStands() && 0.0D == entity.motionX && 0.0D == entity.motionY && 0.0D == entity.motionZ && Minecraft.getMinecraft().thePlayer.ridingEntity != entity) {
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
}
