package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import gg.skytils.skytilsmod.Skytils;

final class SendMessageQueue {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private SendMessageQueue() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean isMessageOrCommandQueuedToBeSentByUser(@NotNull final String messageOrCommand) {
        return Skytils.sendMessageQueue.contains(messageOrCommand);
    }

    static final void queueUserSentMessageOrCommand(@NotNull final String messageOrCommand) {
        Skytils.sendMessageQueue.add(messageOrCommand);
    }
}
