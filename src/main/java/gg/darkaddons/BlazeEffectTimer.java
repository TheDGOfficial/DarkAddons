package gg.darkaddons;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

final class BlazeEffectTimer extends SimpleGuiElement {
    private static final long SMOLDERING_POLARIZATION_DURATION_MS = TimeUnit.HOURS.toMillis(1L);
    private static final long WISPS_ICE_FLAVORED_SPLASH_POTION_DURATION_MS = TimeUnit.MINUTES.toMillis(30L);
    @NotNull
    private static final BooleanSupplier isEnabled = Config::isBlazeEffectTimer;

    private static long polarizationEnd;
    private static long icePotionEnd;

    private static long polarizationTimeLeftSeconds;
    private static long icePotionTimeLeftSeconds;

    private static long lastPolarizationTimeLeftSeconds;
    private static long lastIcePotionTimeLeftSeconds;

    private static long lastInSkyblockTime;

    private static int bossesDone;

    private static final void syncToDisk() {
        // No need to save timeLeft or lastTimeLeftSeconds since those are calculated on the fly.
        TinyConfig.setLong("polarizationEnd", BlazeEffectTimer.polarizationEnd);
        TinyConfig.setLong("icePotionEnd", BlazeEffectTimer.icePotionEnd);
        TinyConfig.setLong("lastInSkyblockTime", BlazeEffectTimer.lastInSkyblockTime);
        TinyConfig.setInt("bossesDone", BlazeEffectTimer.bossesDone);
    }

    private static final void syncFromDisk() {
        final var polarizationEndLocal = TinyConfig.getLong("polarizationEnd");
        final var icePotionEndLocal = TinyConfig.getLong("icePotionEnd");
        final var lastInSkyblockTimeLocal = TinyConfig.getLong("lastInSkyblockTime");
        final var bossesDone = TinyConfig.getInt("bossesDone");

        // Require all values to exist (if a config value does not exist the TinyConfig methods will return null), since we save all at the same time. If one of them exists while other(s) do not it means something got corrupted or interrupted during save, or the config got edited manually, regardless, we do not care nor support those edge cases.
        if (null != polarizationEndLocal && null != icePotionEndLocal && null != lastInSkyblockTimeLocal) {
            BlazeEffectTimer.polarizationEnd = System.currentTimeMillis() + (polarizationEndLocal - lastInSkyblockTimeLocal);
            BlazeEffectTimer.icePotionEnd = System.currentTimeMillis() + (icePotionEndLocal - lastInSkyblockTimeLocal);

            BlazeEffectTimer.lastInSkyblockTime = System.currentTimeMillis();
        }

        if (null != bossesDone) {
            BlazeEffectTimer.bossesDone = bossesDone;
        }
    }

    BlazeEffectTimer() {
        super("Blaze Effect Timer", BlazeEffectTimer.isEnabled, SlayerRNGDisplay::isDoingInfernoDemonlordSlayer, () -> 0);

        // Load previously saved values (if exist).
        BlazeEffectTimer.syncFromDisk();

        DarkAddons.registerTickTask("blaze_effect_timer_update", 20, true, this::update);
        DarkAddons.addShutdownTask(BlazeEffectTimer::syncToDisk); // Required for accurate lastInSkyblockTime
    }

    private static final void parseMessage(@NotNull String message) {
        message = Utils.removeControlCodes(message).trim();

        switch (message) {
            case "You ate a Re-heated Gummy Polar Bear!" -> {
                // Re-heated Gummy Polar Bear's can be stacked after a previous game update a while ago.
                // Only the ones stacked after mod was installed will be taken into account.
                // Data will not be saved to the disk in a un-clean exit of the game so that will also make the timer inaccurate.
                BlazeEffectTimer.polarizationEnd = BlazeEffectTimer.polarizationEnd > System.currentTimeMillis() ? BlazeEffectTimer.polarizationEnd + BlazeEffectTimer.SMOLDERING_POLARIZATION_DURATION_MS : System.currentTimeMillis() + BlazeEffectTimer.SMOLDERING_POLARIZATION_DURATION_MS;
                BlazeEffectTimer.bossesDone = 0;
                BlazeEffectTimer.syncToDisk();
            }
            case "BUFF! You splashed yourself with Wisp's Ice-Flavored Water I! Press TAB or type /effects to view your active effects!" -> {
                BlazeEffectTimer.icePotionEnd = System.currentTimeMillis() + BlazeEffectTimer.WISPS_ICE_FLAVORED_SPLASH_POTION_DURATION_MS;
                BlazeEffectTimer.syncToDisk();
            }
        }

        // If someone else splashes you, the message is different and has a player name in it, so we can't check for the exact message, but check if start and end part of the message is correct. We do not care about the in-between part which is name of the player splashing the potion, which is irrelevant.
        if (message.startsWith("BUFF! You were splashed by") && message.endsWith("with Wisp's Ice-Flavored Water I! Press TAB or type /effects to view your active effects!")) {
            BlazeEffectTimer.icePotionEnd = System.currentTimeMillis() + BlazeEffectTimer.WISPS_ICE_FLAVORED_SPLASH_POTION_DURATION_MS;
            BlazeEffectTimer.syncToDisk();
        }

        if (message.startsWith("SLAYER QUEST COMPLETE!")) {
            ++BlazeEffectTimer.bossesDone;
        }
    }

    static final void onMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("blaze_effect_timer_check_message");

        if (BlazeEffectTimer.isEnabled.getAsBoolean() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            final var message = event.message;
            // Check for an existance of a color code that we expect before passing to other method that removes colors and does more expensive equals and starts/ends with checks.
            if (message.getFormattedText().contains("§a")) {
                BlazeEffectTimer.parseMessage(message.getUnformattedText());
            }
        }

        McProfilerHelper.endSection();
    }

    @Override
    final void clear() {
        BlazeEffectTimer.polarizationEnd = 0L;

        BlazeEffectTimer.polarizationTimeLeftSeconds = 0L;
        BlazeEffectTimer.lastPolarizationTimeLeftSeconds = 0L;

        BlazeEffectTimer.icePotionEnd = 0L;

        BlazeEffectTimer.icePotionTimeLeftSeconds = 0L;
        BlazeEffectTimer.lastIcePotionTimeLeftSeconds = 0L;

        BlazeEffectTimer.lastInSkyblockTime = 0L;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        final var isInSkyblock = DarkAddons.isInSkyblock();

        final var currentTime = System.currentTimeMillis();
        if (isInSkyblock) {
            BlazeEffectTimer.lastInSkyblockTime = currentTime;
        }

        // We lose the millisecond precision, but it doesn't matter anyway as we only update every second at most.
        // This needed to ensure proper cache - if it was millisecond precision, lastTimeLeft would basically change every call and caching would be pointless.
        final var polarizationTimeLeftSecondsLocal = Math.max(0L, TimeUnit.MILLISECONDS.toSeconds(BlazeEffectTimer.polarizationEnd - BlazeEffectTimer.lastInSkyblockTime));
        final var icePotionTimeLeftSecondsLocal = Math.max(0L, TimeUnit.MILLISECONDS.toSeconds(BlazeEffectTimer.icePotionEnd - BlazeEffectTimer.lastInSkyblockTime));

        if (!isDemoRenderBypass && !DarkAddons.isPlayerInCrimsonIsle()) {
            if (0L == polarizationTimeLeftSecondsLocal && 0L == icePotionTimeLeftSecondsLocal) {
                this.clear();
            } else if (!isInSkyblock) {
                // Simulate effect timer pausing when out of SB by always making the end variable the current time plus time left variable. Loses us the millisecond precision, but it doesn't matter anyway as we only update every second at most.
                // Due to performance reasons, we are not counting down on any timer but simply saving the end time and calculating a cached time left value instead, so simulation is needed to achieve pausing the timer.
                // Counting down client side would also be less accurate cause of FPS or TPS limitations and server de-sync. So we are simply calculating the end time when the timer starts and then calculating time left based off of current and end date.
                BlazeEffectTimer.polarizationEnd = currentTime + TimeUnit.SECONDS.toMillis(polarizationTimeLeftSecondsLocal);
                BlazeEffectTimer.icePotionEnd = currentTime + TimeUnit.SECONDS.toMillis(icePotionTimeLeftSecondsLocal);
                BlazeEffectTimer.lastInSkyblockTime = currentTime;
            }

            return;
        }

        BlazeEffectTimer.polarizationTimeLeftSeconds = polarizationTimeLeftSecondsLocal;
        BlazeEffectTimer.icePotionTimeLeftSeconds = icePotionTimeLeftSecondsLocal;

        if (isDemoRenderBypass || BlazeEffectTimer.lastPolarizationTimeLeftSeconds != BlazeEffectTimer.polarizationTimeLeftSeconds || BlazeEffectTimer.lastIcePotionTimeLeftSeconds != BlazeEffectTimer.icePotionTimeLeftSeconds || this.isEmpty()) {
            BlazeEffectTimer.lastPolarizationTimeLeftSeconds = BlazeEffectTimer.polarizationTimeLeftSeconds;
            BlazeEffectTimer.lastIcePotionTimeLeftSeconds = BlazeEffectTimer.icePotionTimeLeftSeconds;

            super.update();
        }
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        // Normally, formatTime can show time left values like 500 ms for a short period of time when the time left value is not 0 nor 1 second. However, this does not apply to us since we already convert from seconds to milliseconds it'll either be 0 or 1.
        lines.add(0L == BlazeEffectTimer.polarizationTimeLeftSeconds ? "§cSmoldering Polarization Expired!" : "§aSmoldering Polarization: " + Utils.formatTime(TimeUnit.SECONDS.toMillis(BlazeEffectTimer.polarizationTimeLeftSeconds), true));
        lines.add(0L == BlazeEffectTimer.icePotionTimeLeftSeconds ? "§cWisp's Ice Flavored Splash Potion Expired!" : "§aWisp's Ice Flavored Splash Potion: " + Utils.formatTime(TimeUnit.SECONDS.toMillis(BlazeEffectTimer.icePotionTimeLeftSeconds), true));
        lines.add("");
        lines.add("§eBosses Done Since Smoldering Polarization: §6" + BlazeEffectTimer.bossesDone);
    }
}
