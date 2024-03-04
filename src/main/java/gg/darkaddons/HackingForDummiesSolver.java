package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class HackingForDummiesSolver {
    private HackingForDummiesSolver() {
        super();
    }

    @Nullable
    private static GuiScreen currentGui;
    @Nullable
    private static IInventory currentChest;
    private static boolean isInHackingGui;

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void onGuiOpen(@NotNull final GuiOpenEvent event) {
        McProfilerHelper.startSection("hacking_for_dummies_solver_gui");

        if (Config.isHackingForDummiesSolver()) {
            // Not a memory leak, normally we would register one for GuiCloseEvent
            // as well and set it to null there so that it can be garbage collected,
            // but there's no GuiCloseEvent and instead, GuiOpenEvent is just fired with
            // event.gui as null when you close a GUI. So there's no memory leak here.
            HackingForDummiesSolver.currentGui = event.gui;
        }

        McProfilerHelper.endSection();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void onPlayerTick(@NotNull final TickEvent.PlayerTickEvent event) {
        McProfilerHelper.startSection("hacking_for_dummies_solver_tick");

        if (Config.isHackingForDummiesSolver() && event.side.isClient() && TickEvent.Phase.START == event.phase) {
            final var inventory = Minecraft.getMinecraft().thePlayer.openContainer;
            HackingForDummiesSolver.currentChest = inventory instanceof ContainerChest ? ((ContainerChest) inventory).getLowerChestInventory() : null;
            HackingForDummiesSolver.isInHackingGui = null != HackingForDummiesSolver.currentChest && ("Hacking".equals(HackingForDummiesSolver.currentChest.getName()) || "Hacking (As seen on CSI)".equals(HackingForDummiesSolver.currentChest.getName())) && 54 == HackingForDummiesSolver.currentChest.getSizeInventory() && HackingForDummiesSolver.currentGui instanceof GuiChest;
        }

        McProfilerHelper.endSection();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void onRenderTick(@NotNull final TickEvent.RenderTickEvent event) {
        //McProfilerHelper.startSection("hacking_for_dummies_solver_render");

        if (Config.isHackingForDummiesSolver()) {
            final var current = HackingForDummiesSolver.currentChest;
            if (TickEvent.Phase.END == event.phase && HackingForDummiesSolver.isInHackingGui && null != current) { // Shouldn't be null if isInHackingGui but just in case
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();

                final var minecraft = Minecraft.getMinecraft();

                var scaledWidth = minecraft.displayWidth;
                var scaledHeight = minecraft.displayHeight;
                final var scaleFactor = HackingForDummiesSolver.getScaleFactor(minecraft, scaledWidth, scaledHeight);
                final var scaledWidthD = (double) scaledWidth / scaleFactor;
                final var scaledHeightD = (double) scaledHeight / scaleFactor;
                scaledWidth = MathHelper.ceiling_double_int(scaledWidthD);
                scaledHeight = MathHelper.ceiling_double_int(scaledHeightD);

                // GuiContainer
                final var xSize = 176;
                final var guiLeft = scaledWidth - xSize >> 1;
                // GuiChest
                final var j = 222;
                final var k = j - 108;
                final var inventoryRows = 54 / 9;
                final var ySize = k + inventoryRows * 18;
                final var guiTop = scaledHeight - ySize >> 1;

                GlStateManager.translate(guiLeft + 7.0F, guiTop + 17.0F, 0.0F);
                HackingForDummiesSolver.renderHackable(current, 2, 11, 10, 17);
                HackingForDummiesSolver.renderHackable(current, 3, 21, 18, 25);
                HackingForDummiesSolver.renderHackable(current, 4, 31, 28, 35);
                HackingForDummiesSolver.renderHackable(current, 5, 41, 36, 43);
                HackingForDummiesSolver.renderHackable(current, 6, 51, 46, 53);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.popMatrix();
            }
        }

        //McProfilerHelper.endSection();
    }

    private static final int getScaleFactor(@NotNull final Minecraft minecraft, final int scaledWidth, final int scaledHeight) {
        final var bl = minecraft.isUnicode();
        var i = minecraft.gameSettings.guiScale;
        if (0 == i) {
            i = 1_000;
        }
        var scaleFactor = 1;
        while (scaleFactor < i && 320 <= scaledWidth / (scaleFactor + 1) && 240 <= scaledHeight / (scaleFactor + 1)) {
            ++scaleFactor;
        }
        if (bl && 0 != scaleFactor % 2 && 1 != scaleFactor) {
            --scaleFactor;
        }
        return scaleFactor;
    }

    private static final int getSlotStackSize(@NotNull final IInventory chest, final int slotIndex) {
        final var item = chest.getStackInSlot(slotIndex);
        return null == item ? -1 : item.stackSize;
    }

    private static final void renderHackable(@NotNull final IInventory chest, final int targetSlotIndex, final int clickSlotIndex, final int checkSlotStartIndex, final int checkSlotEndIndex) {
        final var stackSize = HackingForDummiesSolver.getSlotStackSize(chest, targetSlotIndex);

        for (var i = checkSlotStartIndex; i <= checkSlotEndIndex; i++) {
            if (HackingForDummiesSolver.getSlotStackSize(chest, i) == stackSize) {
                if (i == clickSlotIndex) {
                    HackingForDummiesSolver.drawSlot(clickSlotIndex, 0, 255, 0, 127);
                    return;
                }
                HackingForDummiesSolver.drawSlot(i, 0, 0, 255, 127);
                break;
            }
        }
        HackingForDummiesSolver.drawSlot(clickSlotIndex, 255, 0, 0, 127);
    }

    private static final void drawSlot(final int slotIndex, final int color) {
        final var x = slotIndex % 9 * 18;
        final var y = slotIndex / 9 * 18;
        Gui.drawRect(x + 1, y + 1, x + 17, y + 17, color);
    }

    private static final void drawSlot(final int slotIndex, final int red, final int green, final int blue, final int alpha) {
        HackingForDummiesSolver.drawSlot(slotIndex, (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF);
    }
}
