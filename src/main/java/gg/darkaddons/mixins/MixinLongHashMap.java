package gg.darkaddons.mixins;

import gg.darkaddons.MinecraftCollection;

import gg.darkaddons.annotations.bytecode.Name;
import net.minecraft.util.LongHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Arrays;

import java.lang.reflect.Field;

import org.jetbrains.annotations.NotNull;

@Mixin(value = LongHashMap.class, priority = 999)
final class MixinLongHashMap implements MinecraftCollection {
    @Unique
    private static final Field darkaddons$hashArrayField;

    static {
        try {
            darkaddons$hashArrayField = LongHashMap.class.getDeclaredField("field_76169_a"); // hashArray / field_76169_a
            MixinLongHashMap.darkaddons$hashArrayField.setAccessible(true);
        } catch (final Throwable error) {
            throw new RuntimeException("Could not reflect LongHashMap.hashArray", error);
        }
    }

    @Unique
    @NotNull
    private final Object[] getHashArray() {
        try {
            return (Object[]) MixinLongHashMap.darkaddons$hashArrayField.get(this);
        } catch (final Throwable error) {
            throw new RuntimeException("Could not reflect LongHashMap.hashArray", error);
        }
    }

    @Shadow
    private transient int numHashElements;

    private MixinLongHashMap() {
        super();
    }

    @Override
    public final int collectionSize() {
        return this.numHashElements;
    }

    @Override
    public final void clearCollection() {
        Arrays.fill(this.getHashArray(), null);
        this.numHashElements = 0;
    }

    @Override
    @Unique
    @Name("toString$darkaddons")
    public final String toString() {
        return "MixinLongHashMap{" +
            "hashArray=" + Arrays.toString(this.getHashArray()) +
            ", numHashElements=" + this.numHashElements +
            '}';
    }
}
