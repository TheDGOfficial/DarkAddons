package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

final class M7Features {
    @NotNull
    private static final EnumSet<WitherKingDragons> spawningDragons = EnumSet.noneOf(WitherKingDragons.class);

    @NotNull
    static final EnumSet<WitherKingDragons> getSpawningDragons() {
        return M7Features.spawningDragons;
    }

    @NotNull
    private static final EnumSet<WitherKingDragons> killedDragons = EnumSet.noneOf(WitherKingDragons.class);

    @NotNull
    static final EnumSet<WitherKingDragons> getKilledDragons() {
        return M7Features.killedDragons;
    }

    @NotNull
    private static final HashMap<Integer, WitherKingDragons> dragonMap = new HashMap<>(Utils.calculateHashMapCapacity(WitherKingDragons.getValuesLength()));

    private static final int @NotNull [] reverseDragonMap = new int[WitherKingDragons.getValuesLength()];

    static final int @NotNull [] getReverseDragonMap() {
        return M7Features.reverseDragonMap;
    }

    @NotNull
    private static final EnumMap<WitherKingDragons, Long> dragonSpawnTimes = new EnumMap<>(WitherKingDragons.class);

    @NotNull
    static final EnumMap<WitherKingDragons, Long> getDragonSpawnTimes() {
        return M7Features.dragonSpawnTimes;
    }

    M7Features() {
        super();
    }

    static {
        Utils.fillIntArray(M7Features.reverseDragonMap, -1);
    }

    static final void handlePacket(@NotNull final Packet<?> p) {
        // TODO: this method is called outside the Client thread, inspect if modifications to collections are only done here and all other threads only read it or perhaps use concurrent collection variants.
        if (-1L == DungeonTimer.getPhase4ClearTime() || AdditionalM7Features.isWitherKingDefeated() || !AdditionalM7Features.isInM7()) {
            return;
        }

        //noinspection ChainOfInstanceofChecks
        if (Config.isDragonHud() && p instanceof S2CPacketSpawnGlobalEntity) {
            M7Features.handleS2CPacketSpawnGlobalEntity((S2CPacketSpawnGlobalEntity) p);
        } else if ((Config.isSpawningNotification() || Config.isDragonHud()) && p instanceof final S2APacketParticles packet) {

            if (20 != packet.getParticleCount() || !Utils.compareDoubleExact(packet.getYCoordinate(), 19.0) || EnumParticleTypes.FLAME != packet.getParticleType() || !Utils.compareFloatExact(packet.getXOffset(), 2.0F) || !Utils.compareFloatExact(packet.getYOffset(), 3.0F) || !Utils.compareFloatExact(packet.getZOffset(), 2.0F) || 0.0F != packet.getParticleSpeed() || !packet.isLongDistance() || 0.0D != packet.getXCoordinate() % 1.0D || 0.0D != packet.getZCoordinate() % 1.0D) {
                return;
            }

            final var owner = Utils.findElement(WitherKingDragons.getValues(), (final WitherKingDragons dragon) -> Utils.compareDoubleToIntExact(packet.getXCoordinate(), dragon.getParticleLocation().getX()) && Utils.compareDoubleToIntExact(packet.getZCoordinate(), dragon.getParticleLocation().getZ()));

            if (null == owner) {
                return;
            }

            M7Features.dragonSpawnTimes.computeIfAbsent(owner, (final WitherKingDragons dragon) -> {
                if (Config.isSpawningNotification()) {
                    final var color = owner.getChatColor();
                    final var name = owner.getEnumName();

                    GuiManager.queueTitle(color + "§l" + name + " §c§lis spawning!"); // shown for 2 seconds (implementation detail)
                }

                return System.currentTimeMillis() + 5_000L;
            });
        }
    }

    private static final void handleS2CPacketSpawnGlobalEntity(@NotNull final S2CPacketSpawnGlobalEntity p) {
        if (1 == p.func_149053_g()) {
            final var x = p.func_149051_d() / 32.0D;
            final var y = p.func_149050_e() / 32.0D;
            final var z = p.func_149049_f() / 32.0D;

            if (0.0D != x % 1.0D || 0.0D != y % 1.0D || 0.0D != z % 1.0D) {
                return;
            }

            final var drag = Utils.findElement(WitherKingDragons.getValues(), (final WitherKingDragons dragon) -> {
                final var pos = dragon.getBlockPos();

                return Utils.compareDoubleToIntExact(x, pos.getX()) && Utils.compareDoubleToIntExact(z, pos.getZ());
            });

            if (null == drag) {
                return;
            }

            M7Features.spawningDragons.add(drag);
        }
    }

    private static final double getXZDistSq(@NotNull final Entity entity, @NotNull final BlockPos pos) {
        final var xDelta = entity.posX - pos.getX();
        final var zDelta = entity.posZ - pos.getZ();

        return xDelta * xDelta + zDelta * zDelta;
    }

    static final void onMobSpawned(@NotNull final Entity entity) {
        final var hud = Config.isDragonHud();
        if ((hud || Config.isStatueDestroyedNotification() || Config.isStatueMissedNotification()) && -1L != DungeonTimer.getPhase4ClearTime() && entity instanceof final EntityDragon dragon && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
            final var id = dragon.getEntityId();

            var type = M7Features.dragonMap.get(id);

            if (null == type) {
                type = Utils.minValue(WitherKingDragons.getValues(), (final WitherKingDragons drag) -> M7Features.getXZDistSq(dragon, drag.getBlockPos()));

                if (null == type) {
                    return;
                }

                final var entityWitherKingDragon = (EntityWitherKingDragon) dragon;
                entityWitherKingDragon.setWitherKingDragonTypeOrdinal(type.getEnumOrdinal());
            }

            M7Features.dragonMap.put(id, type);
 
            if (hud) {
                M7Features.reverseDragonMap[type.getEnumOrdinal()] = id;

                M7Features.spawningDragons.remove(type);
                M7Features.killedDragons.remove(type);
            }
        }
    }

    private static final void handleDeath(@NotNull final LivingDeathEvent event) {
        final var hud = Config.isDragonHud();
 
        if (!hud && !Config.isStatueDestroyedNotification() && !Config.isStatueMissedNotification()) {
            return;
        }

        final var entity = event.entity;

        if (entity instanceof final EntityDragon dragon) {
            final var type = WitherKingDragons.from(((EntityWitherKingDragon) dragon).getWitherKingDragonTypeOrdinal());

            if (null == type) {
                return;
            }

            M7Features.dragonMap.remove(dragon.getEntityId());

            if (hud) {
                M7Features.killedDragons.add(type);
                M7Features.spawningDragons.remove(type);
                M7Features.reverseDragonMap[type.getEnumOrdinal()] = -1;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onDeath(@NotNull final LivingDeathEvent event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("m7features_handle_death", event, M7Features::handleDeath);
        } else {
            M7Features.handleDeath(event);
        }
    }

    private static final void handleWorldUnload() {
        M7Features.spawningDragons.clear();
        M7Features.killedDragons.clear();
        M7Features.dragonMap.clear();

        final var len = M7Features.reverseDragonMap.length;
        for (var i = 0; i < len; ++i) {
            M7Features.reverseDragonMap[i] = -1;
        }

        for (final var dragon : WitherKingDragons.getValues()) {
            dragon.setDestroyed(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public final void onWorldUnload(@NotNull final WorldEvent.Unload event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("m7features_handle_load", M7Features::handleWorldUnload);
        } else {
            M7Features.handleWorldUnload();
        }
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void handleRender(@NotNull final EntityLivingBase e) {
        final var showStatueBox = Config.isShowStatueBox();
        if ((showStatueBox || Config.isKillNotification()) && -1L != DungeonTimer.getPhase4ClearTime() && e instanceof final EntityDragon dragon && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
            final var drag = WitherKingDragons.from(((EntityWitherKingDragon) dragon).getWitherKingDragonTypeOrdinal());

            if (null != drag) {
                M7Features.renderDragonInfo(e, drag);
            }
        }
    }

    private static final void renderDragonInfo(@NotNull final EntityLivingBase e, @NotNull final WitherKingDragons drag) {
        final var entityX = e.posX;
        final var entityY = e.posY;
        final var entityZ = e.posZ;

        if (Config.isKillNotification() && M7DragonDisplay.isVecInside(drag.getMoreAccurateBoundingBoxIfEnabled(), entityX, entityY, entityZ)) {
            GuiManager.createTitle("§c§lKill " + drag.getChatColor() + "§l" + drag.getEnumName() + "§c§l!", 1, false);
        }
    }

    static final void handleRenderWorld(@NotNull final RenderWorldLastEvent event) {
        McProfilerHelper.startSection("m7features_handle_render_world");

        final var player = Minecraft.getMinecraft().thePlayer;

        if (Config.isShowStatueBox() && (-1L != DungeonTimer.getPhase4ClearTime() || null != player && 45 >= player.getPosition().getY()) && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
            final var width = Config.isSharperDragonBoundingBox() ? Config.getDragonBoundingBoxWidth() : 3.69F;

            for (final var drag : WitherKingDragons.getValues()) {
                if (Config.isHideStatueBoxForDestroyedStatues() && drag.isDestroyed()) {
                    continue;
                }

                RenderUtil.drawOutlinedBoundingBox(
                    drag.getMoreAccurateBoundingBoxIfEnabled(),
                    drag.getColor(),
                    width,
                    event.partialTicks
                );

                if (Config.isShowArrowStackBox()) {
                    RenderUtil.drawOutlinedBoundingBox(
                        drag.getArrowStackBoundingBox(),
                        drag.getColor(),
                        width,
                        event.partialTicks
                    );
                }
            }
        }

        McProfilerHelper.endSection();
    }
}
