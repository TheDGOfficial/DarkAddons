package gg.darkaddons.mixins;

import net.minecraft.util.IntHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin to allow accessing some private fields in the {@link IntHashMap} class.
 */
@Mixin(value = IntHashMap.class, priority = 999)
public interface IMixinIntHashMap {
    /**
     * Gets the count of entries in the map (private field in {@link IntHashMap}).
     */
    @Accessor
    int getCount();
}

