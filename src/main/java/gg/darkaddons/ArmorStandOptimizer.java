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

    private static final boolean isNotOnGoldorWhitelist(@NotNull final String name, final boolean isInF7OrM7) {
        // P3 Active/Inactive Terms, Devices and Levers
        return !isInF7OrM7 || !name.contains("Inactive Terminal") && !name.contains("CLICK HERE") && !name.contains("Not Activated") && !name.contains("Inactive") && !name.contains("Device") && !name.contains("Activated") && !name.contains("Active");
    }

    private static final boolean isNotOnDungeonWhitelist(@NotNull final String name, final boolean isInDungeons) {
        // Quiz Puzzle
        return !isInDungeons || !name.contains("ⓐ") && !name.contains("ⓑ") && !name.contains("ⓒ") && !name.contains("Question #") && !(!name.isEmpty() && '?' == name.charAt(name.length() - 1)) && !name.contains("Which of these");
    }

    private static final boolean isNotOnGeneralWhitelist(@NotNull final String name) {
        return // Barbarian Duke X
            !name.contains("Duke") &&
                // Diana
                !name.contains("damage") && !name.contains("Inquisitor") && !name.contains("Champion") &&
                // Enderman Slayer
                !name.contains("Voidgloom") && !name.contains("Voidling") && !name.contains("Voidcrazed") &&
                // Blaze Slayer
                !name.contains("Demonlord") && !name.contains("Kindleheart") && !name.contains("Burningsoul") && !name.contains("Smoldering") && !name.contains("Millennia-Aged") && !name.contains("Spawned by:") && !name.contains("ⓆⓊⒶⓏⒾⒾ") && !name.contains("ⓉⓎⓅⒽⓄⒺⓊⓈ") && !name.contains("IMMUNE") && !name.contains("SPIRIT") && !name.contains("ASHEN") && !name.contains("CRYSTAL") && !name.contains("AURIC") && !name.contains("Vanquisher") && !name.contains("hits") && !name.contains("Plasmaflux") &&
                // The Matriarch
                !name.contains("COLLECT!") && !name.contains("Heavy Pearl") && !name.contains("Hits Remaining: ") && !name.contains("Punch!") && !name.contains("The Matriarch") && !name.contains("Heavy Pearls Available: ") && !name.contains("Attempt Cooldown: ");
    }

    private static final boolean isNotOnAnyWhitelist(@NotNull final EntityArmorStand entityArmorStand) {
        return ArmorStandOptimizer.isNotOnAnyWhitelist(entityArmorStand, null);
    }

    private static final boolean isNotOnAnyWhitelist(@NotNull final EntityArmorStand entityArmorStand, @Nullable String name) {
        if (null == name) {
            //noinspection AssignmentToMethodParameter
            name = entityArmorStand.getCustomNameTag();
        }
        return ArmorStandOptimizer.isNotOnGeneralWhitelist(name) && ArmorStandOptimizer.isNotOnDungeonWhitelist(name, DarkAddons.isInDungeons()) && ArmorStandOptimizer.isNotOnSadanWhitelist(name, AdditionalM7Features.isInM6OrF6Boss(DungeonTimer.INSTANCE.getBossEntryTime())) && ArmorStandOptimizer.isNotOnGoldorWhitelist(name, AdditionalM7Features.isInM7OrF7());
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

                final var name = ArmorStandOptimizer.getAndClearLastNameTag();

                if (!ArmorStandOptimizer.isNotOnAnyWhitelist(stand, name)) {
                    ArmorStandOptimizer.armorStandRenderSet.add(stand);
                } else {
                    ArmorStandOptimizer.reusableStands.add(stand);
                }
            }
        }

        final var limit = Config.getArmorStandLimit();

        if (ArmorStandOptimizer.reusableStands.size() <= limit) {
            ArmorStandOptimizer.armorStandRenderSet.addAll(ArmorStandOptimizer.reusableStands);
        } else {
            ArmorStandOptimizer.reusableStands.sort(Comparator.comparingDouble(player::getDistanceSqToEntity));
            ArmorStandOptimizer.armorStandRenderSet.addAll(ArmorStandOptimizer.reusableStands.subList(0, limit));
        }
    }

    static {
        DarkAddons.registerTickTask("armor_stand_optimizer_refresh", 1, true, ArmorStandOptimizer::refreshArmorStands);
    }

    static final boolean checkRender(@NotNull final Entity entity) {
        return !Config.isArmorStandOptimizer() || !AdditionalM7Features.canHideArmorstands() || ArmorStandOptimizer.armorStandRenderSet.contains(entity);
    }
}
