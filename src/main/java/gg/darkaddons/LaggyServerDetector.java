package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Locale;

import java.util.Arrays;

final class LaggyServerDetector {
    LaggyServerDetector() {
        super();
    }

    private static final int[] TPS_SAMPLES = new int[30]; // TPS Over 30 seconds as the sample size
    private static int CURRENT_INDEX = 0;

    @SubscribeEvent
    public final void onWorldChange(@NotNull final WorldEvent.Load event) {
        if (Config.isLaggyServerDetector()) {
            final var world = event.world;

            if (null == world || !world.isRemote) {
                return;
            }

            Utils.fillIntArray(LaggyServerDetector.TPS_SAMPLES, 0);
            LaggyServerDetector.CURRENT_INDEX = 0;

            ServerTPSCalculator.startCalculatingTPS(LaggyServerDetector::onTPSUpdate);
        }
    }

    private static final void onTPSUpdate(final int tps) {
        LaggyServerDetector.TPS_SAMPLES[LaggyServerDetector.CURRENT_INDEX++] = tps;

        if (LaggyServerDetector.TPS_SAMPLES.length == LaggyServerDetector.CURRENT_INDEX) {
            LaggyServerDetector.CURRENT_INDEX = 0;
            ServerTPSCalculator.stopCalculatingTPS();

            var totalTicksOver30Seconds = 0;

            for (final var ticks : TPS_SAMPLES) {
                totalTicksOver30Seconds += ticks;
            }

            final var tpsAverageOver30Seconds = ((double) totalTicksOver30Seconds) / LaggyServerDetector.TPS_SAMPLES.length;
            final var tpsAverageOver30SecondsFormatted = String.format(Locale.ROOT, "%.2f", tpsAverageOver30Seconds); // Required or else would display too much precision, bad for human readability.

            var comment = "";
            var color = "";

            final var average = tpsAverageOver30Seconds;

            if (average >= 19.0) {
                comment = " (Good)";
                color = "§a";
            } else if (average >= 18.0) {
                comment = " (Decent)";
                color = "§2";
            } else if (average >= 17.0) {
                comment = " (Fine)";
                color = "§6";
            } else if (average >= 16.0) {
                comment = " (Bearable)";
                color = "§e";
            } else if (average >= 15.0) {
                comment = " (Bad)";
                color = "§c";
            } else {
                comment = " (Very Bad)";
                color = "§4";
            }

            GuiManager.createTitle("§d30s TPS AVG: " + color + tpsAverageOver30SecondsFormatted + comment, 80, true, GuiManager.Sound.ORB);

            if (Config.isLaggyServerDetectorNotifyParty() && DarkAddons.isInDungeons()) {
                DarkAddons.queueUserSentMessageOrCommand("/pc 30 Second TPS Average: " + tpsAverageOver30SecondsFormatted + comment);
            }
        }
    }
}
