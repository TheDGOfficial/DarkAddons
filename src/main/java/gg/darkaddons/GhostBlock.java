package gg.darkaddons;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

final class GhostBlock {
    @NotNull
    private static final KeyBinding CREATE_GHOST_BLOCK_KEY = new KeyBinding("Create Ghost Block", Keyboard.KEY_G, DarkAddons.MOD_NAME);

    GhostBlock() {
        super();
    }

    static final void registerKeybindings() {
        ClientRegistry.registerKeyBinding(GhostBlock.CREATE_GHOST_BLOCK_KEY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onTick(@NotNull final TickEvent.ClientTickEvent event) {
        if (Config.isCreateGhostBlockWithKey() && (GhostBlock.CREATE_GHOST_BLOCK_KEY.isPressed() || Config.isKeepHoldingToCreateMoreGhostBlocks() && GhostBlock.CREATE_GHOST_BLOCK_KEY.isKeyDown())) {
            final var mc = Minecraft.getMinecraft();
            final var object = mc.thePlayer.rayTrace(mc.playerController.getBlockReachDistance(), 1.0F);

            if (null != object) {
                final var blockPos = object.getBlockPos();

                if (null != blockPos) {
                    final var world = mc.theWorld;
                    final var lookingAtblock = world.getBlockState(blockPos).getBlock();

                    if (!GhostBlock.isOnWhitelist(lookingAtblock) && Blocks.air != lookingAtblock) {
                        world.setBlockToAir(blockPos);
                    }
                }
            }
        }
    }

    private static final boolean isOnWhitelist(@NotNull final Block block) {
        return Blocks.chest == block || Blocks.lever == block || Blocks.trapped_chest == block || Blocks.wooden_button == block || Blocks.stone_button == block || Blocks.skull == block || Blocks.bedrock == block;
    }
}
