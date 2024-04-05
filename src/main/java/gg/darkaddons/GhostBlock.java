package gg.darkaddons;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
    public final void onRenderWorld(@NotNull final RenderWorldLastEvent event) {
        if (Config.isCreateGhostBlockWithKey() && GhostBlock.CREATE_GHOST_BLOCK_KEY.isKeyDown()) {
            final var mc = Minecraft.getMinecraft();
            final var object = mc.thePlayer.rayTrace(mc.playerController.getBlockReachDistance(), 1.0F);

            if (null != object) {
                final var blockPos = object.getBlockPos();

                if (null != blockPos) {
                    final var world = mc.theWorld;
                    final var lookingAtblock = world.getBlockState(blockPos).getBlock();

                    if (!GhostBlock.isInteractable(lookingAtblock) && Blocks.air != lookingAtblock) {
                        world.setBlockToAir(blockPos);
                    }
                }
            }
        }
    }

    private static final boolean isInteractable(@NotNull final Block block) {
        return Blocks.chest == block || Blocks.lever == block || Blocks.trapped_chest == block || Blocks.wooden_button == block || Blocks.stone_button == block || Blocks.skull == block;
    }
}
