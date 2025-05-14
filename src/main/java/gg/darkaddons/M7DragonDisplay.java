package gg.darkaddons;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

final class M7DragonDisplay extends GuiElement {
    private static final float @NotNull [] dragonLastKnownHPs = new float[WitherKingDragons.getValuesLength()];
    private static final float @NotNull [] dragonHPsFromScoreboard = new float[WitherKingDragons.getValuesLength()];

    @NotNull
    private static final String SUFFIX = "❤";
    @NotNull
    private static final String DEAD_TEXT = "§cDead";

    private static final long DEMO_TIME_TILL_SPAWN_MS = 4_824L;

    private static final void clearDragonLastKnownHPs() {
        Utils.fillFloatArray(M7DragonDisplay.dragonLastKnownHPs, Float.MAX_VALUE);
    }

    private static final void clearDragonHPsFromScoreboard() {
        Utils.fillFloatArray(M7DragonDisplay.dragonHPsFromScoreboard, Float.MAX_VALUE);
    }

    static {
        M7DragonDisplay.resetHPs();

        DarkAddons.registerTickTask("update_dragon_hps_scoreboard", 4, true, () -> {
            if (!Config.isDragonHud() || !DarkAddons.isInSkyblock() || !DarkAddons.isInDungeons() || -1L == DungeonTimer.getPhase4ClearTime() || !AdditionalM7Features.isInM7()) {
                return;
            }

            M7DragonDisplay.updateDragonHPsFromScoreboard();
        });
    }

    private static final void updateDragonHPsFromScoreboard() {
        for (final var line : ScoreboardUtil.getScoreboardLines()) {
            if (line.contains(M7DragonDisplay.SUFFIX)) {
                final var lineWithoutColors = Utils.removeControlCodes(line);

                for (final var dragon : WitherKingDragons.getValues()) {
                    final var prefix = dragon.getScoreboardPrefix();

                    if (lineWithoutColors.startsWith(prefix)) {
                        final var hp = StringUtils.remove(StringUtils.remove(line, prefix), 'M' + M7DragonDisplay.SUFFIX);
                        final var hpParsed = Utils.safeParseIntFast(StringUtils.remove(hp, 'B' + M7DragonDisplay.SUFFIX));

                        if (-1 != hpParsed) {
                            M7DragonDisplay.dragonHPsFromScoreboard[dragon.getEnumOrdinal()] = hpParsed;
                        }

                        break;
                    }
                }
            }
        }
    }

    private static final float getDragonHpFromScoreboardCached(@NotNull final WitherKingDragons dragon) {
        return M7DragonDisplay.dragonHPsFromScoreboard[dragon.getEnumOrdinal()];
    }

    private static final float getLastKnownDragonHpOrFetchFromScoreboard(@NotNull final WitherKingDragons dragon) {
        final var lastKnownHp = M7DragonDisplay.dragonLastKnownHPs[dragon.getEnumOrdinal()];
        final var scoreboardHp = M7DragonDisplay.getDragonHpFromScoreboardCached(dragon);

        return !Utils.compareFloatExact(lastKnownHp, Float.MAX_VALUE) && scoreboardHp >= lastKnownHp ? lastKnownHp : scoreboardHp;
    }

    @NotNull
    private static final String getDragonInfoText(@NotNull final WitherKingDragons dragon, final boolean demo) {
        return (dragon.isDestroyed() || demo && (WitherKingDragons.ICE == dragon || WitherKingDragons.FLAME == dragon) ? "§a✔" : "§c✖") + ' ' + dragon.getChatColor() + dragon.getEnumName() + "§f: " + M7DragonDisplay.getDragonStatusInfo(dragon, demo);
    }

    @NotNull
    private static final String getHealthText(@NotNull final WitherKingDragons dragon, final boolean demo, final float health, final double maxHealth) {
        if (Utils.compareFloatExact(health, Float.MAX_VALUE) || demo && WitherKingDragons.ICE == dragon) {
            return "§7Not spawned";
        }

        if (0.0F >= health || demo && WitherKingDragons.FLAME == dragon) {
            return M7DragonDisplay.DEAD_TEXT;
        }

        final var percentage = health / maxHealth;
        final String color;

        if (0.75 <= percentage) {
            color = "§a";
        } else if (0.5 <= percentage) {
            color = "§e";
        } else {
            color = 0.25 <= percentage ? "§6" : "§c";
        }

        return color + Utils.formatNumber(health) + " §4❤";
    }

    static final boolean isVecInside(@NotNull final AxisAlignedBB axisAlignedBB, final double xCoord, final double yCoord, final double zCoord) {
        return xCoord > axisAlignedBB.minX && xCoord < axisAlignedBB.maxX && yCoord > axisAlignedBB.minY && yCoord < axisAlignedBB.maxY && zCoord > axisAlignedBB.minZ && zCoord < axisAlignedBB.maxZ;
    }

    @NotNull
    private static final String getDragonStatusInfo(@NotNull final WitherKingDragons dragon, final boolean demo) {
        if (dragon.isAlive() && (!demo || WitherKingDragons.POWER != dragon)) {
            var maxHealth = 1_000_000_000.0;
            var health = 1_000_000_000.0F;

            if (DarkAddons.isDerpy()) {
                maxHealth *= 2.0D;
                health *= 2.0F;
            }

            final double xCoord;
            final double yCoord;
            final double zCoord;

            final var entity = dragon.getEntity();

            if (null != entity && !demo) {
                health = entity.getHealth();
                M7DragonDisplay.dragonLastKnownHPs[dragon.getEnumOrdinal()] = health;

                if (entity.isDead) {
                    return M7DragonDisplay.DEAD_TEXT;
                }

                maxHealth = entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue();

                xCoord = entity.posX;
                yCoord = entity.posY;
                zCoord = entity.posZ;
            } else {
                xCoord = 0.0D;
                yCoord = 0.0D;
                zCoord = 0.0D;
            }

            return !demo && null == entity ? M7DragonDisplay.getHealthText(dragon, false, M7DragonDisplay.getLastKnownDragonHpOrFetchFromScoreboard(dragon), maxHealth) : M7DragonDisplay.getHealthText(dragon, demo, health, maxHealth) + (M7DragonDisplay.isVecInside(dragon.getMoreAccurateBoundingBoxIfEnabled(), xCoord, yCoord, zCoord) || demo && WitherKingDragons.SOUL == dragon ? " §fR" : "");
        }

        if (!dragon.isSpawning() && (!demo || WitherKingDragons.FLAME == dragon)) {
            return M7DragonDisplay.DEAD_TEXT;
        }

        final var timeTillSpawn = demo ? M7DragonDisplay.DEMO_TIME_TILL_SPAWN_MS : dragon.getTimeTillSpawn();
        final char color;

        if (1_000L >= timeTillSpawn) {
            color = 'c';
        } else {
            color = 3_000L >= timeTillSpawn ? 'e' : 'a';
        }

        // Assume spawned and dead if it's spawning in negative time.
        return 0L > timeTillSpawn ? M7DragonDisplay.DEAD_TEXT : "§bSpawning in §" + color + Utils.formatMillisecondsAsSeconds(timeTillSpawn);
    }

    private static final void resetHPs() {
        M7DragonDisplay.clearDragonLastKnownHPs();
        M7DragonDisplay.clearDragonHPsFromScoreboard();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onWorldUnload(@NotNull final WorldEvent.Unload ignoredEvent) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("dragon_display_on_world_load", M7DragonDisplay::resetHPs);
        } else {
            M7DragonDisplay.resetHPs();
        }
    }

    M7DragonDisplay() {
        super("M7 Dragon Display");
    }

    @Override
    final void render(final boolean demo) {
        if (demo || this.isEnabled() && DarkAddons.isInSkyblock() && DarkAddons.isInDungeons() && -1L != DungeonTimer.getPhase4ClearTime() && AdditionalM7Features.isInM7() && !DarkAddons.isInLocationEditingGui()) {
            final var leftAlign = this.shouldLeftAlign();
            final var xPos = leftAlign ? 0.0F : this.getWidth(demo);

            final var shadow = switch (Config.getDragonHudShadow()) {
                case 1 -> SmartFontRenderer.TextShadow.NORMAL;
                case 2 -> SmartFontRenderer.TextShadow.OUTLINE;
                default -> SmartFontRenderer.TextShadow.NONE;
            };

            final var fontHeight = GuiElement.getFontHeight();

            final var drags = WitherKingDragons.getValues();
            final var length = WitherKingDragons.getValuesLength();
            for (var i = 0; i < length; ++i) {
                GuiElement.drawString(
                    M7DragonDisplay.getDragonInfoText(drags[i], demo),
                    xPos,
                    i * fontHeight,
                    leftAlign,
                    shadow
                );
            }
        }
    }

    @Override
    final boolean isEnabled() {
        return Config.isDragonHud();
    }

    @Override
    final int getHeight() {
        return GuiElement.getFontHeight() * WitherKingDragons.getValuesLength();
    }

    @Override
    final int getWidth(final boolean demo) {
        return GuiElement.getTextWidth(M7DragonDisplay.getDragonInfoText(WitherKingDragons.POWER, true));
    }
}
