package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

final class DialogueSkipHelper {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private DialogueSkipHelper() {
        super();

        throw Utils.staticClassException();
    }

    static final void onMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("dialogue_skip_helper_check_message");

        if (Config.isDialogueSkipHelper() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type) && DarkAddons.isInSkyblock() && DarkAddons.isInDungeons()) {
            DialogueSkipHelper.handleMessage(event.message.getUnformattedText());
        }

        McProfilerHelper.endSection();
    }

    private static final void handleMessage(@NotNull final String message) {
        final var clean = Utils.removeControlCodes(message);

        if ("[BOSS] The Watcher: Let's see how you can handle this.".equals(clean)) {
            GuiManager.createTitle("§4Kill Blood Mobs!", 60, true, GuiManager.Sound.PLING);
        }
    }
}
