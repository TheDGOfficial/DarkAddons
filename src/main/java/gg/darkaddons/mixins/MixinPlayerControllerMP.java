package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.annotations.bytecode.Name;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PlayerControllerMP.class, priority = 1_001)
final class MixinPlayerControllerMP {
    @Shadow
    @Nullable
    private ItemStack currentItemHittingBlock;

    @Shadow
    @NotNull
    private BlockPos currentBlock;

    private MixinPlayerControllerMP() {
        super();
    }

    /**
     * Fixes lore (and all other NBT tag) updates on mining tools resetting mining progress.
     *
     * @author TheDGOfficial
     * @reason Without the fix, money from mining is less, because you lose an additional tick (50 milliseconds) per block broken. With maxed Mining Speed from maxed Mining Setup, you are able to mine 1 block in about 10-11 ticks, and losing 1 tick means -10% less profits, and this is gets higher the higher your ping is. Not to mention the need to also re-click LC and aim again; it's probably more than 10%.
     */
    @Overwrite
    private final boolean isHittingPosition(@NotNull final BlockPos pos) {
        return DarkAddons.isHittingPosition(pos, this.currentItemHittingBlock, this.currentBlock);
    }

    @Override
    @Unique
    @Name("toString$darkaddons")
    public final String toString() {
        return "MixinPlayerControllerMP{" +
            "currentItemHittingBlock=" + this.currentItemHittingBlock +
            ", currentBlock=" + this.currentBlock +
            '}';
    }
}
