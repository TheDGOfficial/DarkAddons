package gg.darkaddons;

import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

final class CenturyRaffleTicketTimer extends GuiElement {
    private static final long RAFFLE_READY_TIME_MS = TimeUnit.MINUTES.toMillis(30L);
    private static final long DEMO_TIME = CenturyRaffleTicketTimer.RAFFLE_READY_TIME_MS;

    private static long ptTicketExpireTime = -1L;

    private static boolean ptTicketWasNotInit;
    private static boolean ptTicketWasNegative;

    private static final void updateTimer(@Nullable final String chatText) {
        if (null != chatText && "PLAYTIME! You gained +1 Raffle Ticket!".equals(Utils.removeControlCodes(chatText))) {
            CenturyRaffleTicketTimer.ptTicketExpireTime = System.currentTimeMillis() + CenturyRaffleTicketTimer.RAFFLE_READY_TIME_MS;

            CenturyRaffleTicketTimer.ptTicketWasNotInit = false;
            CenturyRaffleTicketTimer.ptTicketWasNegative = false;
        }
    }

    private static final long getTimeLeftForPtTicketInMs() {
        while (true) {
            final var expireTime = CenturyRaffleTicketTimer.ptTicketExpireTime;

            if (-1L == expireTime) {
                CenturyRaffleTicketTimer.resetTimer(true); // Not initialized, assume 30min. If this was wrong, the message will update it later to the correct value.

                continue;
            }

            final var timeLeftInMs = expireTime - System.currentTimeMillis();

            if (0L > timeLeftInMs) {
                CenturyRaffleTicketTimer.resetTimer(false); // Negative value, reset to 30min. If this was wrong, the message will update it later to the correct value.

                continue;
            }

            return timeLeftInMs;
        }
    }

    private static final void resetTimer(final boolean notInit) {
        CenturyRaffleTicketTimer.ptTicketExpireTime = System.currentTimeMillis() + CenturyRaffleTicketTimer.RAFFLE_READY_TIME_MS;

        if (notInit) {
            CenturyRaffleTicketTimer.ptTicketWasNotInit = true;
        } else {
            CenturyRaffleTicketTimer.ptTicketWasNegative = true;
        }
    }

    CenturyRaffleTicketTimer() {
        super("Century Raffle Ticket Timer");
    }

    @NotNull
    private static final String getDisplayTextForPtTicket(final long timeInMillis) {
        final var totalSeconds = timeInMillis / 1_000L;

        final var minutes = totalSeconds % 3_600L / 60L;
        final var seconds = totalSeconds % 60L;

        final var timeInfo = 1L <= minutes ? minutes + "m" : seconds + "s";
        final var extraInfo = CenturyRaffleTicketTimer.getExtraInfo();

        return "§bPlaytime Ticket§f: §" + CenturyRaffleTicketTimer.getColor(timeInMillis) + timeInfo + extraInfo;
    }

    @NotNull
    private static final String getExtraInfo() {
        return CenturyRaffleTicketTimer.ptTicketWasNotInit ? " §9(Unknown)" : CenturyRaffleTicketTimer.ptTicketWasNegative ? " §a(Was Ready)" : "";
    }

    private static final char getColor(final long timeInMillis) {
        return 0L > timeInMillis ? 'a' : 'e';
    }

    @Override
    final void render(final boolean demo) {
        if (demo || this.isEnabled() && DarkAddons.isInSkyblock() && !DarkAddons.isInLocationEditingGui()) {
            final var ptTimeLeftInMs = CenturyRaffleTicketTimer.getTimeLeftForPtTicketInMs();

            final var leftAlign = this.shouldLeftAlign();

            var changedPt = false;

            if (demo && !CenturyRaffleTicketTimer.ptTicketWasNegative) {
                CenturyRaffleTicketTimer.ptTicketWasNegative = true;
                changedPt = true;
            }

            GuiElement.drawString(
                CenturyRaffleTicketTimer.getDisplayTextForPtTicket(demo ? CenturyRaffleTicketTimer.DEMO_TIME : ptTimeLeftInMs),
                leftAlign ? 0.0F : this.getWidth(demo),
                0.0F,
                leftAlign
            );

            if (changedPt) {
                CenturyRaffleTicketTimer.ptTicketWasNegative = false;
            }
        }
    }

    @Override
    final boolean isEnabled() {
        return Config.isCenturyRaffleTicketTimer();
    }

    @Override
    final int getHeight() {
        return GuiElement.getFontHeight() << 1;
    }

    @Override
    final int getWidth(final boolean demo) {
        var text = CenturyRaffleTicketTimer.getDisplayTextForPtTicket(demo ? CenturyRaffleTicketTimer.DEMO_TIME : CenturyRaffleTicketTimer.getTimeLeftForPtTicketInMs());

        if (!text.endsWith(" §a(Was Ready)")) {
            text += " §a(Was Ready)";
        }

        return GuiElement.getTextWidth(text);
    }

    static final void doCheckMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("century_raffle_ticket_timer_check_message");

        if (Config.isCenturyRaffleTicketTimer() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type) && DarkAddons.isInSkyblock()) {
            CenturyRaffleTicketTimer.updateTimer(event.message.getUnformattedText());
        }

        McProfilerHelper.endSection();
    }
}
