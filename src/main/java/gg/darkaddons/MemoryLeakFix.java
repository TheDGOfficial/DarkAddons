package gg.darkaddons;

import gg.darkaddons.mixins.IMixinWorld;
import gg.darkaddons.mixins.IMixinIntHashMap;
import gg.darkaddons.mixins.IMixinChunkProviderClient;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import net.minecraftforge.event.world.WorldEvent;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.multiplayer.ChunkProviderClient;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;

final class MemoryLeakFix {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     *
     * @implNote The thrown {@link UnsupportedOperationException} will have no
     * message to not waste a {@link String} instance in the constant pool.
     */
    private MemoryLeakFix() {
        super();

        throw Utils.staticClassException();
    }

    private static final int RUN_INTERVAL_IN_TICKS = 600;
    private static final byte @NotNull [] EMPTY_BYTE_ARRAY = new byte[0];
    @NotNull
    private static final EntityPlayer[] EMPTY_ENTITY_PLAYER_ARRAY = new EntityPlayer[0];

    private static int memoryReserveOriginalSize;

    private static final Set<WorldClient> oldWorlds = Collections.newSetFromMap(new WeakHashMap<>(Utils.calculateHashMapCapacity(50)));

    static final ArrayList<WorldClient> getPossiblyLeakedWorlds() {
        final var leaked = new ArrayList<WorldClient>(100);

        for (final var world : MemoryLeakFix.oldWorlds) {
            if (world != Minecraft.getMinecraft().theWorld) {
                leaked.add(world);
            }
        }

        return leaked;
    }

    static final HashMap<String, Integer> findLeakedDataInWorld(@NotNull final WorldClient world, final boolean clear) {
        final var data = new HashMap<String, Integer>(Utils.calculateHashMapCapacity(100));
        final var mixinWorld = (IMixinWorld) world;

        MemoryLeakFix.collect(data, "Entities", world.loadedEntityList, clear);
        MemoryLeakFix.collect(data, "Unloaded Entities", mixinWorld.getUnloadedEntityList(), clear);
        MemoryLeakFix.collect(data, "Player Entities", world.playerEntities, clear);
        MemoryLeakFix.collect(data, "Tile Entities", world.loadedTileEntityList, clear);
        MemoryLeakFix.collect(data, "Tickable Tile Entities", world.tickableTileEntities, clear);
        MemoryLeakFix.collect(data, "Tile Entities Pending Removal", mixinWorld.getTileEntitiesToBeRemoved(), clear);

        final var entityIdMap = mixinWorld.getEntitiesById();
        data.put("Entity ID Map", ((IMixinIntHashMap) entityIdMap).getCount());
        if (clear) {
            entityIdMap.clearMap();
        }

        final var chunkProvider = mixinWorld.getChunkProvider();
        if (chunkProvider instanceof final ChunkProviderClient chunkProviderClient) {
            final var chunkMapping = ((IMixinChunkProviderClient) chunkProviderClient).getChunkMapping();
            if (null != chunkMapping) {
                data.put("Chunk Mappings", ((MinecraftCollection) chunkMapping).collectionSize());
                if (clear) {
                    ((MinecraftCollection) chunkMapping).clearCollection();
                }
            }

            MemoryLeakFix.collect(data, "Loaded Chunks", ((IMixinChunkProviderClient) chunkProviderClient).getChunkListing(), clear);
        }

        MemoryLeakFix.collect(data, "Added Tile Entities", mixinWorld.getAddedTileEntityList(), clear);
        MemoryLeakFix.collect(data, "Weather Effects", mixinWorld.getWeatherEffects(), clear);

        return data;
    }

    private static final <T extends Collection<?>> void collect(@NotNull final HashMap<String, Integer> data, @NotNull final String name, @Nullable final T collection, final boolean clear) {
        if (null != collection) {
            data.put(name, collection.size());
            if (clear) {
                collection.clear();
            }
        }
    }

    static final void onWorldUnload(@NotNull final WorldEvent.Unload event) {
        if (Config.isPatchMemoryLeaks()) {
            final var world = event.world;

            if (world instanceof final WorldClient client) {
                MemoryLeakFix.oldWorlds.add(client);

                MemoryLeakFix.findLeakedDataInWorld(client, true);
            }
        }
    }

    static final void freeUnnecessary() {
        if (Config.isPatchMemoryLeaks() && null != Minecraft.memoryReserve) {
            final var currentSize = Minecraft.memoryReserve.length;

            if (0 != currentSize) {
                MemoryLeakFix.memoryReserveOriginalSize = currentSize;
                Minecraft.memoryReserve = MemoryLeakFix.EMPTY_BYTE_ARRAY;
            }
        }
    }

    static final void restoreReservedMemory() {
        final var originalSize = MemoryLeakFix.memoryReserveOriginalSize;

        //noinspection ArrayEquality
        if (MemoryLeakFix.EMPTY_BYTE_ARRAY == Minecraft.memoryReserve && 0 != originalSize) {
            Minecraft.memoryReserve = new byte[originalSize];
        }
    }

    private static final void periodicClean() {
        if (!Config.isPatchMemoryLeaks()) {
            return;
        }

        final var mc = Minecraft.getMinecraft();
        final var world = mc.theWorld;
        if (null == world) {
            return;
        }

        // We need to iterate over the list of entities while removing it,
        // normally an Iterator would suffice, but World#removeEntityFromWorld
        // implicitly removes entity from playerEntities too, and we can't make it use Iterator#remove instead.
        //
        // There are not too many options other than creating a useless copy array (or list, but an array is more efficient).
        //
        // Benchmarks say that .toArray with a constant empty array is faster than creating a new arraylist, .toArray with an empty array each call
        // and pre-sized array.
        //
        // The only faster ways than .toArray(EMPTY_ARRAY_CONSTANT) are:
        //  - Direct .toArray(), which gives Object[].
        //  - Iterator, avoiding the copy altogether.
        //
        // But we can't do those because we need EntityPlayer[] and we can't do Iterator. Also keep in mind this piece of code
        // runs on the client so the entity list is not as huge as the server's, and it only runs every 30 seconds, not each tick. No need to optimize that much.
        final var playerEntities = world.playerEntities.toArray(MemoryLeakFix.EMPTY_ENTITY_PLAYER_ARRAY);

        for (final var entityPlayer : playerEntities) {
            //noinspection ObjectEquality
            if (mc.thePlayer == entityPlayer || mc.thePlayer.ridingEntity == entityPlayer) {
                continue;
            }

            if (entityPlayer.isDead) {
                world.playerEntities.remove(entityPlayer);
            }

            if (0.0D == entityPlayer.posX && 0.0D == entityPlayer.posY && 0.0D == entityPlayer.posZ) {
                world.removeEntityFromWorld(entityPlayer.getEntityId());
            }
        }
    }

    static final void registerPeriodicClean() {
        DarkAddons.registerTickTask("memory_leak_fix", MemoryLeakFix.RUN_INTERVAL_IN_TICKS, true, MemoryLeakFix::periodicClean);
    }
}
