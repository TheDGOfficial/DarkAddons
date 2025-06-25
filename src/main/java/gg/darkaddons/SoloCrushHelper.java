package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;

final class SoloCrushHelper {
    SoloCrushHelper() {
        super();
    }

    private static boolean firstLightningReceived;
    private static boolean done;

    private static int ticksPassed;

    @SubscribeEvent
    public final void onChat(@NotNull final ClientChatReceivedEvent event) {
        if (Config.isSoloCrushHelper() && !SoloCrushHelper.done && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type) && Utils.removeControlCodes(event.message.getUnformattedText()).trim().contains("Storm's Giga Lightning hit you for ")) {
            if (!SoloCrushHelper.firstLightningReceived) {
                SoloCrushHelper.firstLightningReceived = true;
            } else {
                SoloCrushHelper.firstLightningReceived = false;
                SoloCrushHelper.done = true;

                ServerTPSCalculator.startListeningTicks(SoloCrushHelper::onServerTick);
            }
        }
    }

    @SubscribeEvent
    public final void onWorldUnload(@NotNull final WorldEvent.Unload event) {
        if (Config.isSoloCrushHelper()) {
            SoloCrushHelper.firstLightningReceived = false;
            SoloCrushHelper.done = false;

            SoloCrushHelper.ticksPassed = 0;
        }
    }

    private static final void onServerTick() {
        final var passed = ++ticksPassed;

        if (20 == passed) {
            if (Config.isSoloCrushHelper()) { // If the user turned off the feature before timer is finished (Edge case)
                GuiManager.createTitle("§53", 20, true, GuiManager.Sound.ORB);
            }
        }

        if (40 == passed) {
            if (Config.isSoloCrushHelper()) { // If the user turned off the feature before timer is finished (Edge case)
                GuiManager.createTitle("§52", 20, true, GuiManager.Sound.ORB);
            }
        }

        if (60 == passed) {
            if (Config.isSoloCrushHelper()) { // If the user turned off the feature before timer is finished (Edge case)
                GuiManager.createTitle("§51", 20, true, GuiManager.Sound.ORB);
            }
        }

        if (80 == passed) {
            ServerTPSCalculator.stopListeningTicks();

            if (Config.isSoloCrushHelper()) { // If the user turned off the feature before timer is finished (Edge case)
                GuiManager.createTitle("§5Crush!", 20, true, GuiManager.Sound.PLING);
            }
        }
    }
}
