package gg.darkaddons;

import gg.essential.universal.ChatColor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Locale;

@SuppressWarnings("FieldNotUsedInToString")
enum WitherKingDragons {
    POWER("Red",
        WitherKingDragons.newBlockPos(27, 14, 59),
        WitherKingDragons.newBlockPos(27, 13, 58),
        WitherKingDragons.Constants.RED_AWTCOLOR,
        ChatColor.RED,
        WitherKingDragons.newBlockPos(32, 18, 59)),
    APEX("Green",
        WitherKingDragons.newBlockPos(27, 14, 94),
        WitherKingDragons.newBlockPos(22, 8, 95),
        WitherKingDragons.Constants.GREEN_AWTCOLOR,
        ChatColor.GREEN,
        WitherKingDragons.newBlockPos(32, 19, 94)),
    SOUL(
        "Purple",
        WitherKingDragons.newBlockPos(56, 14, 125),
        WitherKingDragons.newBlockPos(57, 13, 125),
        WitherKingDragons.Constants.PURPLE_AWTCOLOR,
        ChatColor.DARK_PURPLE,
        WitherKingDragons.newBlockPos(56, 18, 128)
    ),
    ICE("Blue",
        WitherKingDragons.newBlockPos(84, 14, 94),
        WitherKingDragons.newBlockPos(84, 16, 95),
        WitherKingDragons.Constants.BLUE_AWTCOLOR,
        ChatColor.AQUA,
        WitherKingDragons.newBlockPos(79, 19, 94)),
    FLAME(
        "Orange",
        WitherKingDragons.newBlockPos(85, 14, 56),
        WitherKingDragons.newBlockPos(87, 8, 62),
        WitherKingDragons.Constants.ORANGE_AWTCOLOR,
        ChatColor.GOLD,
        WitherKingDragons.newBlockPos(80, 19, 56)
    );

    private static final BlockPos newBlockPos(final int x, final int y, final int z) {
        return new BlockPos(x, y, z);
    }

    private static final class Constants {
        /**
         * Private constructor since this class only contains static members.
         * <p>
         * Always throws {@link UnsupportedOperationException} (for when
         * constructed via reflection).
         */
        private Constants() {
            super();

            throw Utils.staticClassException();
        }

        @NotNull
        private static final Color RED_AWTCOLOR = new Color(1.0F, 0.0F, 0.0F);
        @NotNull
        private static final Color GREEN_AWTCOLOR = new Color(0.0F, 1.0F, 0.0F);
        @NotNull
        private static final Color PURPLE_AWTCOLOR = new Color(0.501_960_8F, 0.0F, 0.501_960_8F);
        @NotNull
        private static final Color BLUE_AWTCOLOR = new Color(0.0F, 1.0F, 1.0F);
        @NotNull
        private static final Color ORANGE_AWTCOLOR = new Color(1.0F, 0.498_039_22F, 0.313_725_5F);
    }

    @NotNull
    private static final WitherKingDragons[] values = WitherKingDragons.values();

    @NotNull
    static final WitherKingDragons[] getValues() {
        return WitherKingDragons.values;
    }

    private static final int VALUES_LENGTH = WitherKingDragons.values.length;

    static final int getValuesLength() {
        return WitherKingDragons.VALUES_LENGTH;
    }

    @NotNull
    private final String textColor;

    @NotNull
    final String getTextColor() {
        return this.textColor;
    }

    @NotNull
    private final BlockPos blockPos;

    @NotNull
    final BlockPos getBlockPos() {
        return this.blockPos;
    }

    @NotNull
    private final Color color;

    @NotNull
    final Color getColor() {
        return this.color;
    }

    @NotNull
    private final ChatColor chatColor;

    @NotNull
    final ChatColor getChatColor() {
        return this.chatColor;
    }

    @NotNull
    private final Vec3 bottomChinMiddleVec;

    @NotNull
    final Vec3 getBottomChinMiddleVec() {
        return this.bottomChinMiddleVec;
    }

    @NotNull
    private final String enumName = this.name();

    @NotNull
    final String getEnumName() {
        return this.enumName;
    }

    @NotNull
    private final String scoreboardPrefix = "- " + this.generatePrettyName() + " Dragon ";

    @NotNull
    final String getScoreboardPrefix() {
        return this.scoreboardPrefix;
    }

    private final int enumOrdinal = this.ordinal();

    final int getEnumOrdinal() {
        return this.enumOrdinal;
    }

    //@NotNull
    //private final ResourceLocation texture = new ResourceLocation("skytils", "textures/dungeons/m7/dragon_" + this.enumName.toLowerCase(Locale.ROOT) + ".png"); // TODO change skytils to darkaddons after full split

    /*@NotNull
    final ResourceLocation getTexture() {
        return this.texture;
    }*/

    @NotNull
    private static final AxisAlignedBB getAxis(@NotNull final BlockPos pos) {
        final double x = pos.getX();
        final double y = pos.getY();
        final double z = pos.getZ();

        return new AxisAlignedBB(x - 13.5D, y - 8.0D, z - 13.5D, x + 13.5D, y + 13.5D + 2.0, z + 13.5D);
    }

    @NotNull
    private final AxisAlignedBB boundingBox;
    @NotNull
    private final AxisAlignedBB moreAccurateBoundingBox;

    @NotNull
    private final BlockPos particleLocation;

    @NotNull
    final BlockPos getParticleLocation() {
        return this.particleLocation;
    }

    private boolean destroyed;

    final boolean isDestroyed() {
        return this.destroyed;
    }

    final void setDestroyed(final boolean newDestroyed) {
        this.destroyed = newDestroyed;
    }

    private WitherKingDragons(@NotNull final String dragonTextColor, @NotNull final BlockPos blockPosition, @NotNull final BlockPos moreAccurateBlockPos, @NotNull final Color dragonColor, @NotNull final ChatColor dragonChatColor, @NotNull final BlockPos bottomChin) {
        this.textColor = dragonTextColor;

        this.blockPos = blockPosition;

        this.particleLocation = this.blockPos.up(5);
        this.boundingBox = WitherKingDragons.getAxis(this.blockPos);

        this.moreAccurateBoundingBox = WitherKingDragons.getAxis(moreAccurateBlockPos);

        this.color = dragonColor;
        this.chatColor = dragonChatColor;

        this.bottomChinMiddleVec = new Vec3(bottomChin.getX() + 0.5, bottomChin.getY() + 0.5, bottomChin.getZ() + 0.5);
    }

    @NotNull
    final AxisAlignedBB getMoreAccurateBoundingBoxIfEnabled() {
        return Config.isMoreAccurateDragonBoundingBoxes() ? this.moreAccurateBoundingBox : this.boundingBox;
    }

    @NotNull
    private final String generatePrettyName() {
        final var lowerCase = this.enumName.toLowerCase(Locale.ROOT);

        return lowerCase.substring(0, 1).toUpperCase(Locale.ROOT) + lowerCase.substring(1);
    }

    final boolean isAlive() {
        return !M7Features.getKilledDragons().contains(this) && !M7Features.getSpawningDragons().contains(this);
    }

    final boolean isSpawning() {
        return M7Features.getSpawningDragons().contains(this);
    }

    @Nullable
    final EntityDragon getEntity() {
        final var entityId = M7Features.getReverseDragonMap()[this.enumOrdinal];

        if (-1 != entityId) {
            final var entity = Minecraft.getMinecraft().theWorld.getEntityByID(entityId);

            if (entity instanceof EntityDragon) {
                return (EntityDragon) entity;
            }
        }

        return null;
    }

    final long getTimeTillSpawn() {
        final var time = M7Features.getDragonSpawnTimes().get(this);

        return null == time ? 0L : time - System.currentTimeMillis();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    @NotNull
    public final String toString() {
        return this.enumName;
    }

    // TODO remove these after full split
    @NotNull
    private static final WitherKingDragons[] ordinalToDrag = new WitherKingDragons[WitherKingDragons.VALUES_LENGTH];

    @NotNull
    private static final gg.skytils.skytilsmod.features.impl.dungeons.WitherKingDragons[] ordinalToSkytilsDrag = new gg.skytils.skytilsmod.features.impl.dungeons.WitherKingDragons[WitherKingDragons.VALUES_LENGTH];

    private static final void initOrdinalArrays() {
        for (final var drag : WitherKingDragons.values) {
            WitherKingDragons.ordinalToDrag[drag.enumOrdinal] = drag;
        }

        for (final var skytilsDrag : gg.skytils.skytilsmod.features.impl.dungeons.WitherKingDragons.values()) {
            WitherKingDragons.ordinalToSkytilsDrag[skytilsDrag.ordinal()] = skytilsDrag;
        }
    }

    static {
        WitherKingDragons.initOrdinalArrays();
    }

    @NotNull
    final gg.skytils.skytilsmod.features.impl.dungeons.WitherKingDragons toSkytilsDragonType() {
        return WitherKingDragons.ordinalToSkytilsDrag[this.enumOrdinal];
    }

    @Nullable
    static final WitherKingDragons from(@Nullable final gg.skytils.skytilsmod.features.impl.dungeons.WitherKingDragons skytilsDragonType) {
        return null == skytilsDragonType ? null : WitherKingDragons.ordinalToDrag[skytilsDragonType.ordinal()];
    }
}
