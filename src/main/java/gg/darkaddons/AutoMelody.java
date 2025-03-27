package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

final class AutoMelody {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private AutoMelody() {
        super();

        throw Utils.staticClassException();
    }

    @NotNull
    private static final Item QUARTZ_BLOCK_ITEM = Item.getItemFromBlock(Blocks.quartz_block);

    private static int lastInventoryHashCode;

    static final void handleTick(@NotNull final TickEvent.ClientTickEvent event) {
        McProfilerHelper.startSection("automelody_handle_tick");

        if (Config.isAutoMelody()) {
            final var mc = Minecraft.getMinecraft();
            final var currentScreen = mc.currentScreen;
            if (currentScreen instanceof final GuiChest guiChest) {
                final var container = guiChest.inventorySlots;
                if (container instanceof final ContainerChest content) {
                    final var lowerChestInventory = content.getLowerChestInventory();
                    if (lowerChestInventory.getDisplayName().getUnformattedText().startsWith("Harp ")) {
                        final var inventoryHashCode = content.getInventory().hashCode();
                        if (AutoMelody.lastInventoryHashCode != inventoryHashCode) {
                            AutoMelody.lastInventoryHashCode = inventoryHashCode;

                            final var playerControllerMP = mc.playerController;
                            final var windowId = content.windowId;

                            final var player = mc.thePlayer;

                            final var inventorySize = lowerChestInventory.getSizeInventory();
                            for (var i = 0; i < inventorySize; ++i) {
                                final var item = lowerChestInventory.getStackInSlot(i);
                                //noinspection ObjectEquality
                                if (null != item && AutoMelody.QUARTZ_BLOCK_ITEM == item.getItem()) {
                                    AutoMelody.middleClick(playerControllerMP, windowId, player, i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        McProfilerHelper.endSection();
    }

    private static final void middleClick(@NotNull final PlayerControllerMP playerControllerMP, final int windowId, @NotNull final EntityPlayerSP player, final int i) {
        playerControllerMP.windowClick(windowId, i, 2, 3, player);
    }
}
