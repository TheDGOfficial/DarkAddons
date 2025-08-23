package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Locale;

final class LaggyServerDetector {
    LaggyServerDetector() {
        super();
    }

    private static final int[] TPS_SAMPLES = new int[30]; // TPS Over 30 seconds as the sample size
    private static int currentIndex;

    @SubscribeEvent
    public final void onWorldChange(@NotNull final WorldEvent.Load event) {
        if (Config.isLaggyServerDetector()) {
            final var world = event.world;

            if (null == world || !world.isRemote) {
                return;
            }

            Utils.fillIntArray(LaggyServerDetector.TPS_SAMPLES, 0);
            LaggyServerDetector.currentIndex = 0;

            ServerTPSCalculator.startCalculatingTPS(LaggyServerDetector::onTPSUpdate);
        }
    }

    private static final void onTPSUpdate(final int tps) {
        LaggyServerDetector.TPS_SAMPLES[LaggyServerDetector.currentIndex++] = tps;

        if (LaggyServerDetector.TPS_SAMPLES.length == LaggyServerDetector.currentIndex) {
            LaggyServerDetector.currentIndex = 0;
            ServerTPSCalculator.stopCalculatingTPS();

            var totalTicksOver30Seconds = 0;

            for (final var ticks : LaggyServerDetector.TPS_SAMPLES) {
                totalTicksOver30Seconds += ticks;
            }

            final var tpsAverageOver30Seconds = (double) totalTicksOver30Seconds / LaggyServerDetector.TPS_SAMPLES.length;

            var comment = "";
            var color = "";

            if (19.0 <= tpsAverageOver30Seconds) {
                comment = " (Good)";
                color = "§a";
            } else if (18.0 <= tpsAverageOver30Seconds) {
                comment = " (Decent)";
                color = "§2";
            } else if (17.0 <= tpsAverageOver30Seconds) {
                comment = " (Fine)";
                color = "§6";
            } else if (16.0 <= tpsAverageOver30Seconds) {
                comment = " (Bearable)";
                color = "§e";
            } else if (15.0 <= tpsAverageOver30Seconds) {
                comment = " (Bad)";
                color = "§c";
            } else {
                comment = " (Very Bad)";
                color = "§4";
            }

            // Required or else would display too much precision, bad for human readability.
            final var tpsAverageOver30SecondsFormatted = String.format(Locale.ROOT, "%.2f", tpsAverageOver30Seconds);
            GuiManager.createTitle("§d30s TPS AVG: " + color + tpsAverageOver30SecondsFormatted + comment, 80, true, GuiManager.Sound.ORB);

            if (Config.isLaggyServerDetectorNotifyParty() && DarkAddons.isInDungeons()) {
                DarkAddons.queueUserSentMessageOrCommand("/pc 30 Second TPS Average: " + tpsAverageOver30SecondsFormatted + comment);
            }
        }
    }
}
