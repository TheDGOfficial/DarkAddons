package gg.darkaddons.mixins;

import net.minecraft.network.play.server.S3BPacketScoreboardObjective;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import org.jetbrains.annotations.NotNull;

/**
 * Accessor mixin to allow accessing and modifying some private fields in {@link S3BPacketScoreboardObjective} class.
 */
@Mixin(value = S3BPacketScoreboardObjective.class, priority = 999)
public interface IMixinS3BPacketScoreboardObjective {
    /**
     * Gets the objective value, a private field in {@link S3BPacketScoreboardObjective} class.
     */
    @Accessor
    @NotNull
    String getObjectiveValue();

    /**
     * Sets the objective value, a private field in {@link S3BPacketScoreboardObjective} class.
     */
    @Accessor
    void setObjectiveValue(@NotNull final String value);
}
