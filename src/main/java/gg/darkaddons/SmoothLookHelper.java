package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

@SuppressWarnings("strictfp")
final strictfp class SmoothLookHelper {
    private enum Algorithm {
        INSTANTANEOUS,
        LERP,
        EASE_IN_OUT,
        GRADUAL_MOUSE_MOVEMENT;
    }

    // Allows changing the algorithm on the fly via mod's settings menu (saved to and loaded from the config file)
    @NotNull
    private static final SmoothLookHelper.Algorithm getAlgorithm() {
        // This class is currently only used by Auto Fishing Rod feature
        // If we ever use it from any other class needs adding seperate option to select different algorithm for different use-cases
        final var configSelection = Config.getAutoFishingRodAFKBypassAlgorithm();
        switch (configSelection) {
            case 0:
                return SmoothLookHelper.Algorithm.INSTANTANEOUS;
            case 1:
                return SmoothLookHelper.Algorithm.LERP;
            case 2:
                return SmoothLookHelper.Algorithm.EASE_IN_OUT;
            case 3:
                return SmoothLookHelper.Algorithm.GRADUAL_MOUSE_MOVEMENT;
            default:
                throw new IllegalStateException("auto fishing rod afk bypass algorithm selection out-of supported values range received from config: " + configSelection);
        }
    }

    private static final float LERP_SPEED = 0.1F;
    private static final float GRADUAL_SPEED = 0.05F; // Can be adjusted for smoothness

    private static float targetYaw;
    private static float targetPitch;

    private static boolean done = true; // Prevent unnecessary updates

    static final void setTarget(final float yaw, final float pitch) {
        targetYaw = yaw;
        targetPitch = pitch;

        done = false; // Reset to allow updates
    }

    static final void update() {
        if (!done) {
            final var player = Minecraft.getMinecraft().thePlayer;
            if (null != player) {
                // Save initial yaw and pitch before modifying them
                final var lastYaw = player.rotationYaw;
                final var lastPitch = player.rotationPitch;

                switch (SmoothLookHelper.getAlgorithm()) {
                    case INSTANTANEOUS:
                        player.rotationYaw = SmoothLookHelper.targetYaw;
                        player.rotationPitch = SmoothLookHelper.targetPitch;
                        break;
                    case LERP:
                        player.rotationYaw = SmoothLookHelper.lerp(player.rotationYaw, SmoothLookHelper.targetYaw, SmoothLookHelper.LERP_SPEED);
                        player.rotationPitch = SmoothLookHelper.lerp(player.rotationPitch, SmoothLookHelper.targetPitch, SmoothLookHelper.LERP_SPEED);
                        break;
                    case EASE_IN_OUT:
                        player.rotationYaw = SmoothLookHelper.easeInOut(player.rotationYaw, SmoothLookHelper.targetYaw, SmoothLookHelper.LERP_SPEED);
                        player.rotationPitch = SmoothLookHelper.easeInOut(player.rotationPitch, SmoothLookHelper.targetPitch, SmoothLookHelper.LERP_SPEED);
                        break;
                    case GRADUAL_MOUSE_MOVEMENT:
                        SmoothLookHelper.gradualAdjust(player, SmoothLookHelper.GRADUAL_SPEED);
                        break;
                }

                // If yaw and pitch didn't change after applying the algorithm, we're done
                if (player.rotationYaw == lastYaw && player.rotationPitch == lastPitch) {
                    SmoothLookHelper.done = true;
                }
            }
        }
    }

    private static final float lerp(final float start, final float end, final float alpha) {
        return start + alpha * (end - start);
    }

    private static final float easeInOut(final float start, final float end, final float progress) {
        float t = progress * progress * (3.0F - 2.0F * progress); // Smoothstep easing
        return start + t * (end - start);
    }

    private static final void gradualAdjust(@NotNull final EntityPlayer player, final float speed) {
        var deltaYaw = (targetYaw - player.rotationYaw) * speed;
        var deltaPitch = (targetPitch - player.rotationPitch) * speed;

        if (Math.abs(deltaYaw) < 0.1F) deltaYaw = 0.0F;
        if (Math.abs(deltaPitch) < 0.1F) deltaPitch = 0.0F;

        player.rotationYaw += deltaYaw;
        player.rotationPitch += deltaPitch;
    }
}
