package gg.darkaddons.mixins;

import net.minecraft.client.multiplayer.ChunkProviderClient;

import net.minecraft.util.LongHashMap;

import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Accessor mixin to allow accessing some private fields in the {@link ChunkProviderClient} class.
 */
@Mixin(value = ChunkProviderClient.class, priority = 999)
public interface IMixinChunkProviderClient {
    /**
     * Gets the chunk mapping (private field in {@link ChunkProviderClient}).
     */
    @Accessor
    LongHashMap<Chunk> getChunkMapping();

    /**
     * Gets the chunk list (private field in {@link ChunkProviderClient}).
     */
    @Accessor
    List<Chunk> getChunkListing();

    /**
     * Gets the blank chunk (private field in {@link ChunkProviderClient}).
     */
    @Accessor
    Chunk getBlankChunk();
}

