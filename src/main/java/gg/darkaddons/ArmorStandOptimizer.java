package gg.darkaddons;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
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
    private static int passes;

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
        return -1L != DungeonTimer.INSTANCE.getPhase4ClearTime() && AdditionalM7Features.isInM7();
    }

    static final boolean checkRemoveArmorStand(@NotNull final EntityArmorStand entityArmorStand) {
        final var dungeonTimerInstance = DungeonTimer.INSTANCE;
        final var bossEntryTime = dungeonTimerInstance.getBossEntryTime();
        return AdditionalM7Features.canHideArmorstands(dungeonTimerInstance, bossEntryTime) && (ArmorStandOptimizer.isInM7P5() || AdditionalM7Features.isInM6OrF6Boss(bossEntryTime) && ArmorStandOptimizer.isNotOnSadanWhitelist(entityArmorStand.getCustomNameTag(), true));
    }

    @Nullable
    private static final String getAndClearLastNameTag() {
        final var name = NameTagCache.getLastNameTag();
        NameTagCache.clearLastNameTag();

        return name;
    }

    private static final boolean shouldDoBlankRemoval() {
        if (Config.isRemoveBlankArmorStands()) {
            ++ArmorStandOptimizer.passes;
            if (RemoveBlankArmorStands.BLANK_ARMOR_STAND_REMOVAL_INTERVAL_IN_TICKS <= ArmorStandOptimizer.passes) {
                ArmorStandOptimizer.passes = 0;
                return true;
            }
        }
        return false;
    }

    private static final boolean removeBlankArmorStand(@NotNull final WorldClient world, @NotNull final Entity e) {
        McProfilerHelper.startSection("remove_blank_armor_stands");
        if (RemoveBlankArmorStands.removeIfBlankArmorStand(world, e)) {
            McProfilerHelper.endSection();
            NameTagCache.clearLastNameTag();
            return true;
        }
        McProfilerHelper.endSection();
        return false;
    }

    private static final void refreshArmorStands() {
        if (!Config.isArmorStandOptimizer()) {
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

        final var shouldDoBlankRemoval = ArmorStandOptimizer.shouldDoBlankRemoval();

        ArmorStandOptimizer.reusableStands.clear();
        ArmorStandOptimizer.armorStandRenderSet.clear();

        for (final var entity : world.loadedEntityList) {
            if (entity instanceof final EntityArmorStand stand) {
                if (shouldDoBlankRemoval && ArmorStandOptimizer.removeBlankArmorStand(world, entity)) {
                    continue;
                }

                NameTagCache.clearLastNameTag();
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
    }

    static {
        DarkAddons.registerTickTask("armor_stand_optimizer_refresh", 1, true, ArmorStandOptimizer::refreshArmorStands);
    }

    static final boolean checkRender(@NotNull final Entity entity) {
        return !Config.isArmorStandOptimizer() || !AdditionalM7Features.canHideArmorstands() || ArmorStandOptimizer.armorStandRenderSet.contains(entity);
    }
}
