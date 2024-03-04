package gg.darkaddons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.NotNull;

final class RemoveArmorStands {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private RemoveArmorStands() {
        super();

        throw Utils.staticClassException();
    }

    static final void onRenderEntityPre(@NotNull final Entity entity) {
        McProfilerHelper.startSection("darkaddons_remove_armor_stands");
        if (Config.isRemoveArmorStandsOnWitherKingAndSadanFight() && !entity.isDead && entity instanceof EntityArmorStand && ArmorStandOptimizer.checkRemoveArmorStand((EntityArmorStand) entity)) {
            entity.setDead();
            /*if (Config.isDebugMode()) {
                DarkAddons.debug(() -> "Killing armor stand with name " + entity.getName() + "§r§e at x=" + entity.posX + ",y=" + entity.posY + ",z=" + entity.posZ);
            }*/
        }
        McProfilerHelper.endSection();
    }
}
