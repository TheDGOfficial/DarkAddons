package gg.darkaddons;

import gg.darkaddons.mixin.MixinUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class AutoDance {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private AutoDance() {
        super();

        throw Utils.staticClassException();
    }

    private static final void resetState() {
        MixinUtils.setSneakOverride(false);
        MixinUtils.setJumpOverride(false);
        MixinUtils.setPunchOverride(false);

        MixinUtils.setSneakOverridePrecondition(() -> false);
        MixinUtils.setJumpOverridePrecondition(() -> false);
        MixinUtils.setPunchOverridePrecondition(() -> false);
    }

    private static final boolean isOnDanceBlock(final boolean orJumping) {
        final var blockBelow = AutoDance.getBlockUnderEntity(Minecraft.getMinecraft().thePlayer);

        final var standingOnGlassBlock = blockBelow instanceof BlockStainedGlass;
        final var standingOnGlassBlockOrJumping = standingOnGlassBlock || blockBelow instanceof BlockAir;

        return orJumping ? standingOnGlassBlockOrJumping : standingOnGlassBlock;
    }

    @NotNull
    private static final Block getBlockUnderEntity(@NotNull final Entity entity) {
        final var blockX = MathHelper.floor_double(entity.posX);
        final var blockY = MathHelper.floor_double(entity.getEntityBoundingBox().minY) - 1;
        final var blockZ = MathHelper.floor_double(entity.posZ);

        return entity.worldObj.getBlockState(new BlockPos(blockX, blockY, blockZ)).getBlock();
    }

    static final void handleSubTitleUpdate(@Nullable final String subTitle) {
        if (!Config.isAutoDance()) {
            return;
        }

        AutoDance.resetState();

        if (null != subTitle && !subTitle.contains("Move")) {
            if (subTitle.contains("Sneak")) {
                MixinUtils.setSneakOverride(true);
                MixinUtils.setSneakOverridePrecondition(() -> AutoDance.isOnDanceBlock(true));
            }

            if (subTitle.contains("Jump") && !subTitle.contains("Don't jump")) {
                // TODO we fail a lot with the reason "You weren't mid-air!", figure out how many ticks after the title exactly to jump and only jump 1 time instead of multiple.
                MixinUtils.setJumpOverride(true);
                MixinUtils.setJumpOverridePrecondition(() -> AutoDance.isOnDanceBlock(/*false*/true) && (Config.isAggressiveJump() || 0.0D == Minecraft.getMinecraft().thePlayer.motionX && 0.0D == Minecraft.getMinecraft().thePlayer.motionZ)); // This so that we don't jump while the player is moving (makes player get off of block).
            }

            if (!subTitle.contains("Sneak") && !subTitle.contains("Jump") && !subTitle.contains("Don't jump") && !subTitle.contains("Stand") && subTitle.contains("Punch!")) {
                MixinUtils.setPunchOverride(true);
                MixinUtils.setPunchOverridePrecondition(() -> AutoDance.isOnDanceBlock(true));
            }
        }
    }
}
