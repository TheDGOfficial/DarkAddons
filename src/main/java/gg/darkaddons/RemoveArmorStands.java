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
        if (Config.isRemoveArmorStandsOnWitherKingAndSadanFight() && !entity.isDead && entity instanceof EntityArmorStand && ArmorStandOptimizer.checkRemoveArmorStand((EntityArmorStand) entity)) {
            entity.setDead();
        }
    }
}
