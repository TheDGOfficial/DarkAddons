package gg.darkaddons;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

final class ArmorStandOptimizer {
    private static final int UPDATE_INTERVAL = 5;

    private static final long MINIMUM_UPDATE_INTERVAL = 250L;
    private static final long TIME_MULTIPLIER = 10L;

    private static final int STARTING_ENTITY_SIZE = 100;

    private static final double MAX_DISTANCE = 10_000.0D;

    private static long nextUpdateDontRender;

    private static int passes;

    @SuppressWarnings("StaticCollection")
    @NotNull
    private static final HashMap<UUID, Boolean> renderingEntitiesTemp = new HashMap<>(Utils.calculateHashMapCapacity(ArmorStandOptimizer.STARTING_ENTITY_SIZE));
    @SuppressWarnings("StaticCollection")
    @NotNull
    private static final HashMap<UUID, Boolean> renderingEntities = new HashMap<>(Utils.calculateHashMapCapacity(ArmorStandOptimizer.STARTING_ENTITY_SIZE));

    @SuppressWarnings("StaticCollection")
    @NotNull
    private static final HashMap<UUID, Boolean> dontRender = new HashMap<>(Utils.calculateHashMapCapacity(ArmorStandOptimizer.STARTING_ENTITY_SIZE));

    @NotNull
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

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

    // This method will be called very frequently unless updateInterval is raised a lot,
    // so it should allocate the minimum resources needed and call minimal number of methods.

    // Therefore, it is always open for more optimizations.

    @SuppressWarnings("AssignmentToMethodParameter")
    private static final void updateDontRender(@NotNull final WorldClient world, final double x, final double y, final double z, final long startTime, Object[]... multidimensionalEntitiesArray) {
        @SuppressWarnings("LocalVariableNamingConvention") var multidimensionalEntitiesArraySize = multidimensionalEntitiesArray.length;

        @SuppressWarnings({"UnnecessaryLocalVariable", "LocalVariableNamingConvention"}) final var renderingEntitiesLocal = ArmorStandOptimizer.renderingEntities;
        final var dontRenderLocal = ArmorStandOptimizer.dontRender;

        final var shouldDoBlankRemoval = ArmorStandOptimizer.shouldDoBlankRemoval();

        // TODO use a util library or something to get the closest armor stands to the player instead of manual reinvention?
        var hitLimit = false;
        for (final var e : world.loadedEntityList) {
            if (e instanceof final EntityArmorStand entity) {
                if (shouldDoBlankRemoval && ArmorStandOptimizer.removeBlankArmorStand(world, e)) {
                    continue;
                }

                final var name = ArmorStandOptimizer.getAndClearLastNameTag();

                final var uuid = entity.getUniqueID();

                if (Utils.toPrimitive(renderingEntitiesLocal.get(uuid), true)) {
                    final var dist = ArmorStandOptimizer.getDistanceToArmorStandFromCoords(x, y, z, entity);

                    if (ArmorStandOptimizer.MAX_DISTANCE > dist) {
                        final var distInt = ArmorStandOptimizer.convertDistToInt(Math.sqrt(dist));

                        if (distInt >= multidimensionalEntitiesArraySize) {
                            final var minSize = distInt + 1;

                            multidimensionalEntitiesArray = ArmorStandOptimizer.allocEntityArray(hitLimit ? Math.min(minSize + ArmorStandOptimizer.STARTING_ENTITY_SIZE, minSize << 1) : minSize);
                            multidimensionalEntitiesArraySize = multidimensionalEntitiesArray.length;

                            hitLimit = true;
                        }

                        multidimensionalEntitiesArray[distInt] = new Object[]{entity, ArmorStandOptimizer.getArrayNonNull(multidimensionalEntitiesArray[distInt])};
                    } else {
                        if (ArmorStandOptimizer
                            .isNotOnAnyWhitelist(entity, name)) {
                            dontRenderLocal.put(uuid, true); // TODO change to render pass-list instead of blocklist, maybe, to store fewer values? i.e., if there are 500 armor stands, the map will contain 450 entries right now, meanwhile, with reversed logic, it'll only contain 50.
                        } else {
                            dontRenderLocal.remove(uuid);
                        }
                    }
                } else {
                    dontRenderLocal.remove(uuid);
                }
            }
        }

        ArmorStandOptimizer.updateDontRender0(startTime, multidimensionalEntitiesArray, multidimensionalEntitiesArraySize, dontRenderLocal);
    }

    private static final int convertDistToInt(final double squareRoot) {
        return !Double.isNaN(squareRoot) && !Double.isInfinite(squareRoot) ? (int) (0.0D < squareRoot ? Math.floor(squareRoot) : Math.ceil(squareRoot)) : 0;
    }

    @SuppressWarnings("NonReproducibleMathCall")
    private static final double getDistanceToArmorStandFromCoords(final double x, final double y, final double z, @NotNull final EntityArmorStand entity) {
        return Math.pow(Math.pow(Math.pow(x - entity.posX, 2.0 + (y - entity.posY)), 2.0 + (z - entity.posZ)), 2.0);
    }

    @Nullable
    private static final String getAndClearLastNameTag() {
        final var name = NameTagCache.getLastNameTag();
        NameTagCache.clearLastNameTag();

        return name;
    }

    @NotNull
    private static final Object[] getArrayNonNull(final @Nullable Object... multidimensionalEntitiesArray) {
        return null == multidimensionalEntitiesArray ? new Object[1] : multidimensionalEntitiesArray;
    }

    private static final boolean shouldDoBlankRemoval() {
        if (Config.isRemoveBlankArmorStands()) {
            ++ArmorStandOptimizer.passes;
            if (RemoveBlankArmorStands.BLANK_ARMOR_STAND_REMOVAL_INTERVAL_IN_TICKS / ArmorStandOptimizer.UPDATE_INTERVAL <= ArmorStandOptimizer.passes) {
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

    @NotNull
    private static final Object[][] allocEntityArray(final int size) {
        return new Object[size][];
    }

    private static final void updateDontRender0(final long startTime, @NotNull final Object[][] multidimensionalEntitiesArray, final int multidimensionalEntitiesArraySize, @NotNull @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened", "BoundedWildcard"}) final HashMap<UUID, Boolean> dontRenderLocal) {
        var entityCount = 0;
        final var maxEntitiesToRender = Config.getArmorStandLimit();

        @SuppressWarnings("UnnecessaryLocalVariable") final var emptyArr = ArmorStandOptimizer.EMPTY_OBJECT_ARRAY; // avoid get-field op-code inside loop

        for (var i = 0; i < multidimensionalEntitiesArraySize; ++i) {
            var entityCountTemp = entityCount;

            while (true) {
                final var ent = multidimensionalEntitiesArray[i];

                if (null == ent || 0 == ent.length) {
                    break;
                }

                if (ent[0] instanceof final EntityArmorStand entityArmorStand) {
                    final var uuid = entityArmorStand.getUniqueID();

                    if (entityCount > maxEntitiesToRender && ArmorStandOptimizer.isNotOnAnyWhitelist(entityArmorStand)) {
                        dontRenderLocal.put(uuid, true);
                    } else {
                        dontRenderLocal.remove(uuid);
                    }
                }

                multidimensionalEntitiesArray[i] = emptyArr;
                ++entityCountTemp;
            }

            entityCount = entityCountTemp;
        }

        final var endTime = System.currentTimeMillis();

        final var timeTook = endTime - startTime; // future ref: usually takes 0-1 ms but can take up to 10ms
        ArmorStandOptimizer.nextUpdateDontRender = endTime + ArmorStandOptimizer.MINIMUM_UPDATE_INTERVAL + ArmorStandOptimizer.TIME_MULTIPLIER * timeTook;
    }

    private static final void refreshArmorStands() {
        if (!Config.isArmorStandOptimizer()) {
            return;
        }

        final var mc = Minecraft.getMinecraft();
        final var player = mc.thePlayer;

        if (null != player) {
            final var startTime = System.currentTimeMillis();

            if (startTime >= ArmorStandOptimizer.nextUpdateDontRender) {
                ArmorStandOptimizer.updateDontRender(mc.theWorld, player.posX, player.posY, player.posZ, startTime, ArmorStandOptimizer.allocEntityArray(ArmorStandOptimizer.STARTING_ENTITY_SIZE));
            }
        }
    }

    static {
        DarkAddons.registerTickTask("armor_stand_optimizer_refresh", ArmorStandOptimizer.UPDATE_INTERVAL, true, ArmorStandOptimizer::refreshArmorStands);
    }

    private static final void markRenderingEntity(@NotNull final UUID uuid) {
        ArmorStandOptimizer.renderingEntitiesTemp.put(uuid, true);
    }

    private static final boolean shouldRender(@NotNull final UUID uuid) {
        return Utils.toPrimitive(ArmorStandOptimizer.dontRender.get(uuid));
    }

    private static final boolean shouldHide(@NotNull final UUID uuid) {
        ArmorStandOptimizer.markRenderingEntity(uuid);

        return ArmorStandOptimizer.shouldRender(uuid);
    }

    @NotNull
    private static final UUID getUUID(@NotNull final Entity entity) {
        return entity.getUniqueID();
    }

    private static final boolean doCheckRender0(@NotNull final Entity entity) {
        if (AdditionalM7Features.canHideArmorstands() && ArmorStandOptimizer.shouldHide(ArmorStandOptimizer.getUUID(entity))) {
            return false;
        }
        return true;
    }

    static final boolean checkRender(@NotNull final Entity entity) {
        return ArmorStandOptimizer.doCheckRender0(entity);
    }

    private static final void clearRenderingEntities(@SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened"}) @NotNull final HashMap<UUID, Boolean> paramRenderingEntities) {
        paramRenderingEntities.clear();
    }

    private static final void clearRenderingEntitiesTemp(@SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened"}) @NotNull final HashMap<UUID, Boolean> paramRenderingEntitiesTemp) {
        paramRenderingEntitiesTemp.clear();
    }

    private static final void markAllUuidsAsRead(@SuppressWarnings("TypeMayBeWeakened") @NotNull final Set<UUID> uuids, @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened", "BoundedWildcard"}) @NotNull final HashMap<UUID, Boolean> paramRenderingEntities) {
        for (final var uuid : uuids) {
            paramRenderingEntities.put(uuid, true);
        }
    }

    static final void renderWorld() {
        McProfilerHelper.startSection("armor_stand_optimizer_render_world");

        if (Config.isArmorStandOptimizer()) {
            @SuppressWarnings("LocalVariableNamingConvention") final var renderingEntitiesLocal = ArmorStandOptimizer.renderingEntities;
            ArmorStandOptimizer.clearRenderingEntities(renderingEntitiesLocal);
            @SuppressWarnings("LocalVariableNamingConvention") final var renderingEntitiesTempLocal = ArmorStandOptimizer.renderingEntitiesTemp;

            ArmorStandOptimizer.markAllUuidsAsRead(renderingEntitiesTempLocal.keySet(), renderingEntitiesLocal);

            ArmorStandOptimizer.clearRenderingEntitiesTemp(renderingEntitiesTempLocal);
        }

        McProfilerHelper.endSection();
    }
}
