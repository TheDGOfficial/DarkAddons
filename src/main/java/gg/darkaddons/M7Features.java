package gg.darkaddons;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase;
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

    /*@SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened"})
    @NotNull
    private static final HashSet<AxisAlignedBB> glowstones = new HashSet<>(100);*/

    @NotNull
    private static final EnumMap<WitherKingDragons, Long> dragonSpawnTimes = new EnumMap<>(WitherKingDragons.class);

    @NotNull
    static final EnumMap<WitherKingDragons, Long> getDragonSpawnTimes() {
        return M7Features.dragonSpawnTimes;
    }

    M7Features() {
        super();
    }

    /*@SuppressWarnings("ObjectEquality")
    private static final void handleBlockChange(@NotNull final BlockChangeEvent event) {
        if (!Config.isDimensionalSlashAlert() || -1L == DungeonTimer.INSTANCE.getPhase4ClearTime() || AdditionalM7Features.isWitherKingDefeated() || !AdditionalM7Features.isInM7()) {
            return;
        }

        final Block old = event.getOld().getBlock();

        if (old == Blocks.glowstone) {
            M7Features.glowstones.clear();
            return;
        }

        if (event.getUpdate().getBlock() == Blocks.glowstone && old != Blocks.packed_ice) {
            final BlockPos pos = event.getPos();

            M7Features.glowstones.add(new AxisAlignedBB(pos.add(-5, -5, -5), pos.add(5, 5, 5)));
        }
    }*/

    /*@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onBlockChange(@NotNull final BlockChangeEvent event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("m7features_handle_block_change", event, M7Features::handleBlockChange);
        } else {
            M7Features.handleBlockChange(event);
        }
    }*/

    static {
        Utils.fillIntArray(M7Features.reverseDragonMap, -1);

        /*DarkAddons.registerTickTask("check_dimensional_slash", 15, true, () -> {
            final Minecraft mc = Minecraft.getMinecraft();
            final EntityPlayerSP player = mc.thePlayer;

            if (Config.isDimensionalSlashAlert() && -1L != DungeonTimer.INSTANCE.getPhase4ClearTime() && -1L == DungeonTimer.INSTANCE.getScoreShownAt() && null != player && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
                final double x = player.posX;
                final double y = player.posY;
                final double z = player.posZ;

                if (Utils.isAnyMatch(M7Features.glowstones, (final AxisAlignedBB axisAlignedBB) -> M7DragonDisplay.isVecInside(axisAlignedBB, x, y, z))) {
                    UChat.chat(Utils.chromaIfEnabledOrAqua() + "Dimensional slash!");
                    GuiManager.createTitle("Dimensional Slash!", 20);
                }
            }
        });*/
    }

    /*private static final int getAliveDragonCount() {
        int counter = 0;

        for (final int entityId : M7Features.reverseDragonMap) {
            if (-1 != entityId) {
                ++counter;
            }
        }

        return counter;
    }*/

    static final void handlePacket(@NotNull final Packet<?> p) {
        // TODO: this method is called outside the Client thread, inspect if modifications to collections are only done here and all other threads only read it or perhaps use concurrent collection variants.
        if (-1L == DungeonTimer.INSTANCE.getPhase4ClearTime() || AdditionalM7Features.isWitherKingDefeated() || !AdditionalM7Features.isInM7()) {
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
                /*if (Config.isWitherKingFightFailNotification() && 4 == M7Features.getAliveDragonCount()) {
                    GuiManager.createTitle(
                        "§4§lKill a Dragon!",
                        "§c5th Dragon is spawning!",
                        60,
                        60,
                        true,
                        GuiManager.Sound.ANVIL_LAND
                    );
                }*/

                if (Config.isSpawningNotification()) {
                    final var color = owner.getChatColor();
                    final var name = owner.getEnumName();

                    //UChat.chat("§c§lThe " + color + "§l" + name + " §c§ldragon is spawning!");
                    //if (!owner.isDestroyed()) {
                    GuiManager.queueTitle(color + "§l" + name + " §c§lis spawning!"); // shown for 2 seconds (implementation detail)
                    //}
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

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onMobSpawn(@NotNull final EntityJoinWorldEvent event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        M7Features.onMobSpawned(event.entity);
    }

    private static final void onMobSpawned(@NotNull final Entity entity) {
        if (Config.isDragonHud() && -1L != DungeonTimer.INSTANCE.getPhase4ClearTime() && entity instanceof EntityDragon && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
            final var id = entity.getEntityId();

            var type = M7Features.dragonMap.get(id);

            if (null == type) {
                type = Utils.minValue(WitherKingDragons.getValues(), (final WitherKingDragons drag) -> M7Features.getXZDistSq(entity, drag.getBlockPos()));

                if (null == type) {
                    return;
                }

                final var ext = (ExtensionEntityLivingBase) entity;

                ext.getSkytilsHook().setColorMultiplier(type.getColor());
                ext.getSkytilsHook().setMasterDragonType(type.toSkytilsDragonType());
            }

            M7Features.dragonMap.put(id, type);
            M7Features.reverseDragonMap[type.getEnumOrdinal()] = id;

            M7Features.spawningDragons.remove(type);
            M7Features.killedDragons.remove(type);

            /*if (Config.isWitherKingFightFailNotification() && 4 == M7Features.getAliveDragonCount()) {
                GuiManager.createTitle("§c§l4 Drags Alive!", 60, true, GuiManager.Sound.PLING);
            }*/
        }
    }

    private static final void handleDeath(@NotNull final LivingDeathEvent event) {
        if (!Config.isDragonHud()) {
            return;
        }

        final var entity = event.entity;

        if (entity instanceof EntityDragon) {
            final var type = WitherKingDragons.from(((ExtensionEntityLivingBase) entity).getSkytilsHook().getMasterDragonType());

            if (null == type) {
                return;
            }

            M7Features.killedDragons.add(type);
            M7Features.spawningDragons.remove(type);
            M7Features.dragonMap.remove(entity.getEntityId());
            M7Features.reverseDragonMap[type.getEnumOrdinal()] = -1;
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

        //M7Features.glowstones.clear();

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
        //final boolean showDragonHP = Config.isShowDragonHP();
        final var showStatueBox = Config.isShowStatueBox();
        if ((/*showDragonHP || */showStatueBox || Config.isKillNotification()) && -1L != DungeonTimer.INSTANCE.getPhase4ClearTime() && e instanceof EntityDragon && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
            final var drag = WitherKingDragons.from(((ExtensionEntityLivingBase) e).getSkytilsHook().getMasterDragonType());

            if (null != drag) {
                M7Features.renderDragonInfo(e, drag/*, showDragonHP, showStatueBox*/);
            }
        }
    }

    private static final void renderDragonInfo(@NotNull final EntityLivingBase e, @NotNull final WitherKingDragons drag/*, final boolean showDragonHP, final boolean showStatueBox*/) {
        //final float hp = e.getHealth();
        //final double percentage = hp / e.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue();

        final var entityX = e.posX;
        final var entityY = e.posY;
        final var entityZ = e.posZ;

        //final float partialTicks = RenderUtil.getPartialTicks();
        //final boolean isInStatue = M7DragonDisplay.isVecInside(drag.getMoreAccurateBoundingBoxIfEnabled(), entityX, entityY, entityZ);

        /*if (showDragonHP) {
            GlStateManager.disableCull();
            GlStateManager.disableDepth();

            RenderUtil.drawNametag(
                RenderUtil.interpolate(entityX, e.lastTickPosX, partialTicks),
                RenderUtil.interpolate(entityY, e.lastTickPosY, partialTicks) + 0.5D,
                RenderUtil.interpolate(entityZ, e.lastTickPosZ, partialTicks),
                drag.getTextColor() + ' ' + Utils.formatNumber(hp) + (showStatueBox && isInStatue ? " §fR" : ""),
                0.75 <= percentage ? ColorFactory.INSTANCE.getYELLOWGREEN() : 0.5 <= percentage ? ColorFactory.INSTANCE.getYELLOW() : 0.25 <= percentage ? ColorFactory.INSTANCE.getDARKORANGE() : ColorFactory.INSTANCE.getCRIMSON(),
                partialTicks,
                new UMatrixStack(),
                false,
                6.0F,
                false
            );

            GlStateManager.enableCull();
            GlStateManager.enableDepth();
        }*/

        if (Config.isKillNotification() && M7DragonDisplay.isVecInside(drag.getMoreAccurateBoundingBoxIfEnabled(), entityX, entityY, entityZ)/* && !drag.isDestroyed()*/) {
            GuiManager.createTitle("§c§lKill " + drag.getChatColor() + "§l" + drag.getEnumName() + "§c§l!", 1, false);
        }
    }

    static final void handleRenderWorld(@NotNull final RenderWorldLastEvent event) {
        McProfilerHelper.startSection("m7features_handle_render_world");

        final var player = Minecraft.getMinecraft().thePlayer;

        if (Config.isShowStatueBox() && (-1L != DungeonTimer.INSTANCE.getPhase4ClearTime() || null != player && 45 >= player.getPosition().getY()) && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
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
            }
        }

        /*if (Config.isShowDragonSpawnTimer() && -1L != DungeonTimer.INSTANCE.getPhase4ClearTime() && !AdditionalM7Features.isWitherKingDefeated() && AdditionalM7Features.isInM7()) {
            GlStateManager.disableCull();
            GlStateManager.disableDepth();

            final UMatrixStack stack = new UMatrixStack();
            M7Features.dragonSpawnTimes.entrySet().removeIf((final Map.Entry<WitherKingDragons, Long> entry) -> {
                final WitherKingDragons drag = entry.getKey();
                final Long time = entry.getValue();

                final long diff = time - System.currentTimeMillis();

                final char color;

                if (1_000L >= diff) {
                    color = 'c';
                } else {
                    color = 3_000L >= diff ? 'e' : 'a';
                }

                //noinspection StringConcatenationMissingWhitespace
                RenderUtil.drawLabel(
                    drag.getBottomChinMiddleVec(),
                    drag.getTextColor() + ": §" + color + diff + " ms",
                    drag.getColor(),
                    event.partialTicks,
                    stack,
                    false,
                    6.0F
                );

                return 0L > diff;
            });

            GlStateManager.enableCull();
            GlStateManager.enableDepth();
        }*/

        McProfilerHelper.endSection();
    }

    /*private static final void checkRender(final CheckRenderEntityEvent<Entity> event) {
        final Entity entity = event.getEntity();

        if (entity instanceof EntityDragon && 1 < ((EntityDragon) entity).deathTicks && M7Features.shouldHideDragonDeath()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onCheckRender(final CheckRenderEntityEvent<Entity> event) {
        DarkAddons.handleEvent("m7features_check_render", event, M7Features::checkRender);
    }*/

    /*static final float getHurtOpacity(final RenderDragon renderDragon, final EntityDragon lastDrag, final float value) {
        if (!Config.isChangeHurtColorOnDragons()) {
            return value;
        }

        final ExtensionEntityLivingBase lastDragon = (ExtensionEntityLivingBase) lastDrag;

        if (null == lastDragon.getSkytilsHook().getColorMultiplier()) {
            return value;
        }

        final ModelBase mod = renderDragon.getMainModel();
        final AccessorModelDragon model = (AccessorModelDragon) mod;

        model.getBody().isHidden = true;
        model.getWing().isHidden = true;

        return 0.03F;
    }

    static final void getEntityTexture(final EntityDragon entity, final CallbackInfoReturnable<? super ResourceLocation> cir) {
        if (!Config.isRetextureDragons()) {
            return;
        }

        final WitherKingDragons type = WitherKingDragons.from(((ExtensionEntityLivingBase) entity).getSkytilsHook().getMasterDragonType());

        if (null == type) {
            return;
        }

        if (type.isDestroyed() && Config.isSkipRetexturingIrrelevantDrags()) {
            return;
        }

        cir.setReturnValue(type.getTexture());
    }

    static final void afterRenderHurtFrame(final RenderDragon renderDragon) {
        final ModelBase mod = renderDragon.getMainModel();
        final AccessorModelDragon model = (AccessorModelDragon) mod;

        model.getBody().isHidden = false;
        model.getWing().isHidden = false;
    }

    static final boolean shouldHideDragonDeath() {
        return DarkAddons.isInDungeons() && -1L != DungeonTimer.INSTANCE.getPhase4ClearTime() && AdditionalM7Features.isInM7() && !AdditionalM7Features.isWitherKingDefeated() && Config.isHideDragonDeath();
    }*/
}
