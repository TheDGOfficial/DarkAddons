package gg.darkaddons;

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
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;

final class ReplaceDiorite {
    ReplaceDiorite() {
        super();
    }

    @NotNull
    private static final IBlockState[] glassStates = new IBlockState[16];

    static {
        for (var i = 0; i < 16; ++i) {
            glassStates[i] = Blocks.stained_glass.getStateFromMeta(i);
        }
    }

    @NotNull
    private static final BlockPos[] pillars = new BlockPos[] {new BlockPos(46, 169, 41), new BlockPos(46, 169, 65), new BlockPos(100, 169, 65), new BlockPos(100, 169, 41)};
 
    @NotNull
    private static final int[] pillarColors = new int[] {5, 4, 10, 14};

    @NotNull
    private static final HashSet<BlockPos>[] coordinates = ReplaceDiorite.createPillarSets();

    @NotNull
    private static final HashSet<BlockPos>[] createPillarSets() {
        return JavaUtils.createGenericArray(ReplaceDiorite.createPillarSet(0), ReplaceDiorite.createPillarSet(1), ReplaceDiorite.createPillarSet(2), ReplaceDiorite.createPillarSet(3));
    }

    @NotNull
    private static final HashSet<BlockPos> createPillarSet(final int pillarIndex) {
        final var pillar = ReplaceDiorite.pillars[pillarIndex];
        final var set = new HashSet<BlockPos>(16);
        for (var x = pillar.getX() - 3; x <= pillar.getX() + 3; ++x) {
            for (var y = pillar.getY(); y <= pillar.getY() + 37; ++y) {
                for (var z = pillar.getZ() - 3; z <= pillar.getZ() + 3; ++z) {
                    set.add(new BlockPos(x, y, z));
                }
            }
        }
        return set;
    }

    @SubscribeEvent
    public final void onTick(@NotNull final ClientTickEvent event) {
        if (Config.isReplaceDiorite() && TickEvent.Phase.END == event.phase && -1L != DungeonTimer.INSTANCE.getBossEntryTime() && -1L == DungeonTimer.INSTANCE.getPhase2ClearTime()) {
            ReplaceDiorite.replaceDiorite();
        }
    }

    private static final void replaceDiorite() {
        final var world = Minecraft.getMinecraft().theWorld;
        if (null != world) {
            final var chunkProvider = world.getChunkProvider();
            final var chunks = new HashMap<Long, Chunk>(16);
            for (final var coordinate : coordinates) {
                for (final var pos : coordinate) {
                    final long chunkX = pos.getX() >> 4L;
                    final long chunkZ = pos.getZ() >> 4L;

                    final var chunk = chunks.computeIfAbsent((chunkX << 32L) | chunkZ, (k) -> chunkProvider.provideChunk((int) chunkX, (int) chunkZ));
                    if (chunk.getBlock(pos) == Blocks.stone) {
                        ReplaceDiorite.setGlass(world, pos, coordinate);
                    }
                }
            }
        }
    }

    private static final void setGlass(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final HashSet<BlockPos> coordinate) {
        var i = 0;
        var index = 0;
        for (final var coord : coordinates) {
            if (coordinate == coord) {
                index = i;
                break;
            }
            ++i;
        }
        world.setBlockState(pos, glassStates[pillarColors[index]], 3);
    }
}