package gg.darkaddons;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

final class RejoinCooldownDisplay extends SimpleGuiElement {
    @NotNull
    private static final long COOLDOWN_MS = TimeUnit.MINUTES.toMillis(1L);
    @NotNull
    private static final BooleanSupplier isEnabled = Config::isSbRejoinCooldownAfterKickDisplay;

    private static long kickCooldownEnd;
    private static long timeLeftSeconds;
    private static long lastTimeLeftSeconds;

    RejoinCooldownDisplay() {
        super("Rejoin Cooldown Display", RejoinCooldownDisplay.isEnabled, () -> !DarkAddons.isInSkyblock() && 0L != RejoinCooldownDisplay.kickCooldownEnd, () -> 0);

        DarkAddons.registerTickTask("sb_rejoin_cooldown_after_kick_display_update", 20, true, this::update);
    }

    private static final void parseMessage(@NotNull String message) {
        message = Utils.removeControlCodes(message).trim();

        if ("You were kicked while joining that server!".equals(message) || "There was a problem joining SkyBlock, try again in a moment!".equals(message)) {
            RejoinCooldownDisplay.kickCooldownEnd = System.currentTimeMillis() + RejoinCooldownDisplay.COOLDOWN_MS;
        }
    }

    static final void onMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("sb_rejoin_cooldown_after_kick_display_check_message");

        if (RejoinCooldownDisplay.isEnabled.getAsBoolean() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            final var message = event.message;
            if (message.getFormattedText().startsWith("§c")) {
                RejoinCooldownDisplay.parseMessage(message.getUnformattedText());
            }
        }

        McProfilerHelper.endSection();
    }

    @Override
    final void clear() {
        RejoinCooldownDisplay.kickCooldownEnd = 0L;

        RejoinCooldownDisplay.timeLeftSeconds = 0L;
        RejoinCooldownDisplay.lastTimeLeftSeconds = 0L;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        if (!isDemoRenderBypass && DarkAddons.isInSkyblock()) {
            this.clear();
            return;
        }

        RejoinCooldownDisplay.timeLeftSeconds = Math.max(0L, TimeUnit.MILLISECONDS.toSeconds(RejoinCooldownDisplay.kickCooldownEnd - System.currentTimeMillis()));

        if (isDemoRenderBypass || RejoinCooldownDisplay.lastTimeLeftSeconds != RejoinCooldownDisplay.timeLeftSeconds) {
            RejoinCooldownDisplay.lastTimeLeftSeconds = RejoinCooldownDisplay.timeLeftSeconds;

            super.update();
        }
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        lines.add("§cCan rejoin SkyBlock in " + RejoinCooldownDisplay.timeLeftSeconds + 's');
    }
}
