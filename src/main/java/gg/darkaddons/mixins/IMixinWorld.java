package gg.darkaddons.mixins;

import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

import net.minecraft.util.IntHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import org.jetbrains.annotations.NotNull;

/**
 * Accessor mixin to allow accessing some private and protected fields in the {@link World} class.
 */
@Mixin(value = World.class, priority = 999)
public interface IMixinWorld {
    /**
     * Gets the unloaded entity list (protected field in {@link World}).
     */
    @Accessor
    @NotNull
    List<Entity> getUnloadedEntityList();

    /**
     * Gets the tile entities pending removal (private field in {@link World}).
     */
    @Accessor
    @NotNull
    List<TileEntity> getTileEntitiesToBeRemoved();

    /**
     * Gets the tile entities pending addition (private field in {@link World}).
     */
    @Accessor
    @NotNull
    List<TileEntity> getAddedTileEntityList();

    /**
     * Gets the entities by ID map (protected field in {@link World}).
     */
    @Accessor
    @NotNull
    IntHashMap<Entity> getEntitiesById();

    /**
     * Gets the chunk provider (protected field in {@link World}).
     */
    @Accessor
    @NotNull
    IChunkProvider getChunkProvider();

    /**
     * Gets the weather effect entities (private field in {@link World}).
     */
    @Accessor
    @NotNull
    List<Entity> getWeatherEffects();
}

