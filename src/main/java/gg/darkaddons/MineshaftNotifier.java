package gg.darkaddons;

import gg.skytils.skytilsmod.Skytils;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.jetbrains.annotations.NotNull;

final class MineshaftNotifier {
    @NotNull
    private static final String MINESHAFT_MESSAGE = "WOW! You found a Glacite Mineshaft portal!";

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private MineshaftNotifier() {
        super();

        throw Utils.staticClassException();
    }

    static final void doCheckMessage(@NotNull final ClientChatReceivedEvent event) {
        if (Config.isMineshaftNotifier() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            final var formattedMessage = event.message.getFormattedText();
            final var unformattedMessage = Utils.removeControlCodes(event.message.getUnformattedText()).trim();

            if (formattedMessage.contains("§") && MineshaftNotifier.MINESHAFT_MESSAGE.equals(unformattedMessage)) {
                GuiManager.createTitle("§a✔ Found Mineshaft!", "§b§lGood Job!", 60, 60, true, GuiManager.Sound.LEVEL_UP);
                if (Config.isMineshaftNotifierNotifyParty()) {
                    Skytils.sendMessageQueue.add("/pc " + MineshaftNotifier.MINESHAFT_MESSAGE);
                }
            }
        }
    }
}
