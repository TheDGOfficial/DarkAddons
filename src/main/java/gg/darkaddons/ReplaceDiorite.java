package gg.darkaddons;

import gg.darkaddons.mixins.IMixinChunkProviderClient;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.block.state.IBlockState;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.fml.common.gameevent.TickEvent;

final class ReplaceDiorite {
    ReplaceDiorite() {
        super();
    }

    @NotNull
    private static final IBlockState[] glassStates = new IBlockState[16];

    static {
        final var len = ReplaceDiorite.glassStates.length;

        for (var i = 0; len > i; ++i) {
            ReplaceDiorite.glassStates[i] = Blocks.stained_glass.getStateFromMeta(i);
        }
    }

    // 7448 total block positions
    @NotNull
    private static final HashMap<BlockPos, Integer> posToColor = new HashMap<>(Utils.calculateHashMapCapacity(7448));

    // 4 chunks total
    @NotNull
    private static final HashMap<Long, HashSet<BlockPos>> chunkToPositions = new HashMap<>(Utils.calculateHashMapCapacity(4));

    static {
        ReplaceDiorite.addPillar(new BlockPos(46, 169, 41), 5);
        ReplaceDiorite.addPillar(new BlockPos(46, 169, 65), 4);
        ReplaceDiorite.addPillar(new BlockPos(100, 169, 65), 10);
        ReplaceDiorite.addPillar(new BlockPos(100, 169, 41), 14);
    }

    private static final void addPillar(@NotNull final BlockPos origin, final int color) {
        final var pillarX = origin.getX();
        final var pillarY = origin.getY();
        final var pillarZ = origin.getZ();

        for (var x = pillarX - 3; x <= pillarX + 3; ++x) {
            for (var y = pillarY; y <= pillarY + 37; ++y) {
                for (var z = pillarZ - 3; z <= pillarZ + 3; ++z) {
                    final var pos = new BlockPos(x, y, z);
                    ReplaceDiorite.posToColor.put(pos, color);

                    final long key = ((long) (x >> 4) << 32) | ((long) z >> 4 & 0xFFFFFFFFL);
                    ReplaceDiorite.chunkToPositions.computeIfAbsent(key, k -> new HashSet<>(1862)).add(pos);
                }
            }
        }
    }

    @SubscribeEvent
    public final void onTick(@NotNull final TickEvent.ClientTickEvent event) {
        if (Config.isReplaceDiorite() && TickEvent.Phase.END == event.phase && -1L != DungeonTimer.getBossEntryTime() && -1L == DungeonTimer.getPhase2ClearTime()) {
            ReplaceDiorite.replaceDiorite();
        }
    }

    private static final void replaceDiorite() {
        final var world = Minecraft.getMinecraft().theWorld;
        if (null == world) {
            return;
        }

        final var chunkProvider = world.getChunkProvider();
        final var blankChunk = ((IMixinChunkProviderClient) chunkProvider).getBlankChunk();

        for (final var entry : ReplaceDiorite.chunkToPositions.entrySet()) {
            final var key = entry.getKey();
            final var chunkX = (int) (key >> 32);
            final var chunkZ = (int) (key & 0xFFFFFFFFL);

            final var chunk = chunkProvider.provideChunk(chunkX, chunkZ);

            if (blankChunk == chunk) {
                // Skip not loaded chunk
                continue;
            }

            for (final var pos : entry.getValue()) {
                if (Blocks.stone == chunk.getBlock(pos)) {
                    ReplaceDiorite.setGlass(world, pos);
                }
            }
        }
    }

    private static final void setGlass(@NotNull final World world, @NotNull final BlockPos pos) {
        final var color = ReplaceDiorite.posToColor.get(pos);
        if (null != color) {
            world.setBlockState(pos, ReplaceDiorite.glassStates[color], 3);
        }
    }
}

