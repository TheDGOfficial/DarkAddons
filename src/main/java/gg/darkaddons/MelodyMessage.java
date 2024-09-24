package gg.darkaddons;

import gg.skytils.skytilsmod.Skytils;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.client.event.GuiOpenEvent;

import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.inventory.GuiChest;

import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;

final class MelodyMessage {
    private static boolean saidMelody;

    private static boolean quarter;
    private static boolean halfway;
    private static boolean almost;

    MelodyMessage() {
        super();

        DarkAddons.registerTickTask("melody_message", 1, true, MelodyMessage::onTick);
    }

    @SubscribeEvent
    public final void onGuiLoad(@NotNull final GuiOpenEvent event) {
        final var gui = event.gui;
        if (null == gui) {
            MelodyMessage.saidMelody = false;

            MelodyMessage.quarter = false;
            MelodyMessage.halfway = false;
            MelodyMessage.almost = false;
        } else if (Config.isSendMessageOnMelodyTerminal() && !MelodyMessage.saidMelody && DarkAddons.isInDungeons() && gui instanceof final GuiChest chest && chest.inventorySlots instanceof final ContainerChest container && "Click the button on time!".equals(container.getLowerChestInventory().getDisplayName().getUnformattedText())) {
            Skytils.sendMessageQueue.add("/pc Melody Terminal start!");
            MelodyMessage.saidMelody = true;

            MelodyMessage.quarter = false;
            MelodyMessage.halfway = false;
            MelodyMessage.almost = false;
        }
    }

    private static final void onTick() {
        if (!Config.isSendMessageOnMelodyTerminal()) {
            return;
        }

        final var player = Minecraft.getMinecraft().thePlayer;

        if (null == player) {
            return;
        }

        final var openContainer = player.openContainer;

        if (openContainer instanceof final ContainerChest containerChest && "Click the button on time!".equals(containerChest.getLowerChestInventory().getDisplayName().getUnformattedText())) {
            if (!MelodyMessage.quarter && MelodyMessage.checkSlot(containerChest.getSlot(25))) {
                Skytils.sendMessageQueue.add("/pc Melody terminal is at 25%");
                MelodyMessage.quarter = true;
            }
            if (!MelodyMessage.halfway && MelodyMessage.checkSlot(containerChest.getSlot(34))) {
                Skytils.sendMessageQueue.add("/pc Melody terminal is at 50%");
                MelodyMessage.halfway = true;
            }
            if (!MelodyMessage.almost && MelodyMessage.checkSlot(containerChest.getSlot(43))) {
                Skytils.sendMessageQueue.add("/pc Melody terminal is at 75%");
                MelodyMessage.almost = true;
            }
        }
    }

    private static final boolean checkSlot(@Nullable final Slot slot) {
        if (null != slot) {
            final var stack = slot.getStack();

            return null != stack && 5 == stack.getMetadata();
        }

        return false;
    }

    @SubscribeEvent
    public final void onWorldUnLoad(@NotNull final WorldEvent.Unload event) {
        MelodyMessage.saidMelody = false;

        MelodyMessage.quarter = false;
        MelodyMessage.halfway = false;
        MelodyMessage.almost = false;
    }
}
