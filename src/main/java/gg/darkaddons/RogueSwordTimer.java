package gg.darkaddons;

import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

final class RogueSwordTimer extends GuiElement {
    private static final long ROGUE_SWORD_EXPIRE_TIME_MS = TimeUnit.SECONDS.toMillis(30L);
    private static final long DEMO_TIME = RogueSwordTimer.ROGUE_SWORD_EXPIRE_TIME_MS;

    private static long expireTime = -1L;

    private static final void updateTimer(@Nullable final String actionbarText) {
        if (null != actionbarText && Utils.removeControlCodes(actionbarText).contains(" Mana (Speed Boost)")) {
            RogueSwordTimer.expireTime = System.currentTimeMillis() + RogueSwordTimer.ROGUE_SWORD_EXPIRE_TIME_MS;
        }
    }

    private static final long getTimeLeftInMs() {
        if (-1L == RogueSwordTimer.expireTime) {
            return 0L; // Not initialized, assume expired.
        }

        final var timeLeftInMs = RogueSwordTimer.expireTime - System.currentTimeMillis();
        return Math.max(0L, timeLeftInMs); // Negative means expired
    }

    RogueSwordTimer() {
        super("Rogue Sword Timer");
    }

    @NotNull
    private static final String getDisplayText(final long timeInMillis) {
        return "§bRogue Sword§f: §" + RogueSwordTimer.getColor(timeInMillis) + Utils.formatMillisecondsAsSeconds(timeInMillis);
    }

    private static final char getColor(final long timeInMillis) {
        return 0L < timeInMillis ? 'c' : 'a';
    }

    @Override
    final void render(final boolean demo) {
        if (demo || this.isEnabled() && DarkAddons.isInSkyblock() && (Config.isRogueSwordTimerOutOfDungeons() || DarkAddons.isInDungeons()) && !DarkAddons.isInLocationEditingGui()) {
            final var timeLeftInMs = RogueSwordTimer.getTimeLeftInMs();

            if (!Config.isHideRogueSwordTimerOnceZero() || 0L != timeLeftInMs) {
                final var leftAlign = this.shouldLeftAlign();
                GuiElement.drawString(
                    RogueSwordTimer.getDisplayText(demo ? RogueSwordTimer.DEMO_TIME : timeLeftInMs),
                    leftAlign ? 0.0F : this.getWidth(demo),
                    0.0F,
                    leftAlign
                );
            }
        }
    }

    @Override
    final boolean isEnabled() {
        return Config.isRogueSwordTimer();
    }

    @Override
    final int getHeight() {
        return GuiElement.getFontHeight();
    }

    @Override
    final int getWidth(final boolean demo) {
        return GuiElement.getTextWidth(RogueSwordTimer.getDisplayText(demo ? RogueSwordTimer.DEMO_TIME : RogueSwordTimer.getTimeLeftInMs()));
    }

    static final void doCheckMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("rogue_sword_timer_check_message");

        if (Config.isRogueSwordTimer() && MessageType.STATUS_MESSAGE_DISPLAYED_ABOVE_ACTIONBAR.matches(event.type) && DarkAddons.isInSkyblock() && (DarkAddons.isInDungeons() || Config.isRogueSwordTimerOutOfDungeons())) {
            RogueSwordTimer.updateTimer(event.message.getUnformattedText());
        }

        McProfilerHelper.endSection();
    }
}
