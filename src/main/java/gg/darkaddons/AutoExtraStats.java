package gg.darkaddons;

import gg.skytils.skytilsmod.Skytils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

final class AutoExtraStats {
    AutoExtraStats() {
        super();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onClientChatReceived(@NotNull final ClientChatReceivedEvent event) {
        if (Config.isAutoExtraStats() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            final var formattedMessage = event.message.getFormattedText();
            final var unformattedMessage = Utils.removeControlCodes(event.message.getUnformattedText()).trim();

            if (formattedMessage.contains("§") && "> EXTRA STATS <".equals(unformattedMessage)) {
                Skytils.sendMessageQueue.add("/showextrastats");
            }
        }
    }
}
