package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

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
