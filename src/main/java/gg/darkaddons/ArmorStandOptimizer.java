package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;

import com.google.common.collect.Ordering;

final class ArmorStandOptimizer {
    private static final HashSet<EntityArmorStand> armorStandRenderSet = new HashSet<>(Utils.calculateHashMapCapacity(128));
    private static final ArrayList<EntityArmorStand> reusableStands = new ArrayList<>(128);

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private ArmorStandOptimizer() {
        super();

        throw Utils.staticClassException();
    }

    private static final boolean isNotOnSadanWhitelist(@NotNull final String name, final boolean isInF6OrM6) {
        // F6 and M6 Giants and Sadan
        return !isInF6OrM6 || !name.contains("Giant") && !name.contains("Sadan") && !name.contains("Bigfoot") && !name.contains("L.A.S.R");
    }

    private static final boolean isInM7P5() {
        return -1L != DungeonTimer.getPhase4ClearTime() && AdditionalM7Features.isInM7();
    }

    static final boolean checkRemoveArmorStand(@NotNull final EntityArmorStand entityArmorStand) {
        final var bossEntryTime = DungeonTimer.getBossEntryTime();
        return AdditionalM7Features.canHideArmorstands(bossEntryTime) && (ArmorStandOptimizer.isInM7P5() || AdditionalM7Features.isInM6OrF6Boss(bossEntryTime) && ArmorStandOptimizer.isNotOnSadanWhitelist(entityArmorStand.getCustomNameTag(), true));
    }

    private static final void refreshArmorStands() {
        if (!Config.isArmorStandOptimizer()) {
            ArmorStandOptimizer.reusableStands.clear();
            ArmorStandOptimizer.armorStandRenderSet.clear();
            return;
        }

        final var mc = Minecraft.getMinecraft();

        final var world = mc.theWorld;
        final var player = mc.thePlayer;

        if (null == world || null == player) {
            ArmorStandOptimizer.armorStandRenderSet.clear();
            return;
        }

        ArmorStandOptimizer.reusableStands.clear();
        ArmorStandOptimizer.armorStandRenderSet.clear();

        for (final var entity : world.loadedEntityList) {
            if (entity instanceof final EntityArmorStand stand) {
                ArmorStandOptimizer.reusableStands.add(stand);
            }
        }

        final var limit = Config.getArmorStandLimit();

        if (ArmorStandOptimizer.reusableStands.size() <= limit) {
            ArmorStandOptimizer.armorStandRenderSet.addAll(ArmorStandOptimizer.reusableStands);
        } else {
            final var closest = Ordering
                .from(Comparator.comparingDouble(player::getDistanceSqToEntity))
                .leastOf(ArmorStandOptimizer.reusableStands, limit);

            ArmorStandOptimizer.armorStandRenderSet.addAll(closest);
        }

        ArmorStandOptimizer.reusableStands.clear();
    }

    static {
        DarkAddons.registerTickTask("armor_stand_optimizer_refresh", 1, true, ArmorStandOptimizer::refreshArmorStands);
    }

    static final boolean checkRender(@NotNull final Entity entity) {
        return !Config.isArmorStandOptimizer() || !AdditionalM7Features.canHideArmorstands() || ArmorStandOptimizer.armorStandRenderSet.contains(entity);
    }
}
