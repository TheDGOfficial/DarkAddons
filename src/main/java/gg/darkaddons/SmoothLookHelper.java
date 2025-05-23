package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;

@SuppressWarnings("strictfp")
final strictfp class SmoothLookHelper {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private SmoothLookHelper() {
        super();

        throw Utils.staticClassException();
    }

    private enum Algorithm {
        INSTANTANEOUS() {
            @Override
            final float applyForYaw(final float currentYaw, final float targetYaw) {
                return targetYaw;
            }

            @Override
            final float applyForPitch(final float currentPitch, final float targetPitch) {
                return targetPitch;
            }
        },
        LERP() {
            @Override
            final float applyForYaw(final float currentYaw, final float targetYaw) {
                return SmoothLookHelper.lerp(currentYaw, targetYaw, SmoothLookHelper.LERP_SPEED);
            }

            @Override
            final float applyForPitch(final float currentPitch, final float targetPitch) {
                return SmoothLookHelper.lerp(currentPitch, targetPitch, SmoothLookHelper.LERP_SPEED);
            }
        },
        EASE_IN_OUT() {
            @Override
            final float applyForYaw(final float currentYaw, final float targetYaw) {
                return SmoothLookHelper.easeInOut(currentYaw, targetYaw, SmoothLookHelper.LERP_SPEED);
            }

            @Override
            final float applyForPitch(final float currentPitch, final float targetPitch) {
                return SmoothLookHelper.easeInOut(currentPitch, targetPitch, SmoothLookHelper.LERP_SPEED);
            }
        },
        GRADUAL_MOUSE_MOVEMENT() {
            @Override
            final float applyForYaw(final float currentYaw, final float targetYaw) {
                return SmoothLookHelper.gradualAdjust(currentYaw, targetYaw, SmoothLookHelper.GRADUAL_SPEED);
            }

            @Override
            final float applyForPitch(final float currentPitch, final float targetPitch) {
                return SmoothLookHelper.gradualAdjust(currentPitch, targetPitch, SmoothLookHelper.GRADUAL_SPEED);
            }
        };

        abstract float applyForYaw(final float currentYaw, final float targetYaw);

        abstract float applyForPitch(final float currentPitch, final float targetPitch);
    }

    // Allows changing the algorithm on the fly via mod's settings menu (saved to and loaded from the config file)
    @NotNull
    private static final SmoothLookHelper.Algorithm getAlgorithm() {
        // This class is currently only used by Auto Fishing Rod feature
        // If we ever use it from any other class needs adding seperate option to select different algorithm for different use-cases
        final var configSelection = Config.getAutoFishingRodAFKBypassAlgorithm();
        return switch (configSelection) {
            case 0 -> SmoothLookHelper.Algorithm.INSTANTANEOUS;
            case 1 -> SmoothLookHelper.Algorithm.LERP;
            case 2 -> SmoothLookHelper.Algorithm.EASE_IN_OUT;
            case 3 -> SmoothLookHelper.Algorithm.GRADUAL_MOUSE_MOVEMENT;
            default ->
                throw new IllegalStateException("auto fishing rod afk bypass algorithm selection out-of supported values range received from config: " + configSelection);
        };
    }

    private static final float LERP_SPEED = 0.1F;
    private static final float GRADUAL_SPEED = 0.05F; // Can be adjusted for smoothness

    private static float lastYaw = Float.NaN;
    private static float lastPitch = Float.NaN;

    private static float targetYaw;
    private static float targetPitch;

    private static boolean done = true; // Prevent unnecessary updates

    static final void setTarget(final float yaw, final float pitch) {
        SmoothLookHelper.targetYaw = yaw;
        SmoothLookHelper.targetPitch = pitch;

        SmoothLookHelper.lastYaw = Float.NaN;
        SmoothLookHelper.lastPitch = Float.NaN;

        SmoothLookHelper.done = false; // Reset to allow updates
    }

    static final void update() {
        if (!SmoothLookHelper.done) {
            final var player = Minecraft.getMinecraft().thePlayer;
            if (null != player) {
                // If the player manually moved their head, we cancel the entire process and bail out. Player input should always override automatic control.
                if (!Float.isNaN(SmoothLookHelper.lastYaw) && !Float.isNaN(SmoothLookHelper.lastPitch) && (!Utils.compareFloatExact(player.rotationYaw, SmoothLookHelper.lastYaw) || !Utils.compareFloatExact(player.rotationPitch, SmoothLookHelper.lastPitch))) {
                    SmoothLookHelper.done = true;
                    return;
                }

                // Apply algorithm to yaw and pitch of the player
                final var algorithm = SmoothLookHelper.getAlgorithm();

                player.rotationYaw = algorithm.applyForYaw(player.rotationYaw, SmoothLookHelper.targetYaw);
                player.rotationPitch = algorithm.applyForPitch(player.rotationPitch, SmoothLookHelper.targetPitch);

                // If yaw and pitch do not change after applying the algorithm anymore, we're done
                if (Utils.compareFloatExact(algorithm.applyForYaw(player.rotationYaw, SmoothLookHelper.targetYaw), player.rotationYaw) && Utils.compareFloatExact(algorithm.applyForPitch(player.rotationPitch, SmoothLookHelper.targetPitch), player.rotationPitch)) {
                    SmoothLookHelper.done = true;
                }

                SmoothLookHelper.lastYaw = player.rotationYaw;
                SmoothLookHelper.lastPitch = player.rotationPitch;
            }
        }
    }

    private static final float lerp(final float start, final float end, final float alpha) {
        return start + alpha * (end - start);
    }

    private static final float easeInOut(final float start, final float end, final float progress) {
        final var smoothStep = progress * progress * (3.0F - 2.0F * progress); // Smoothstep easing
        return start + smoothStep * (end - start);
    }

    private static final float gradualAdjust(final float start, final float end, final float speed) {
        var delta = (end - start) * speed;

        if (0.1F > Math.abs(delta)) {
            delta = 0.0F;
        }

        return start + delta;
    }
}
