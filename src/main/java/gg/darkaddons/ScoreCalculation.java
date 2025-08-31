package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gg.essential.elementa.state.State;
import gg.essential.elementa.state.BasicState;

import java.util.function.Function;
import java.lang.ref.WeakReference;

final class ScoreCalculation {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private ScoreCalculation() {
        super();

        throw Utils.staticClassException();
    }

    @NotNull
    private static final WeakReference<gg.skytils.skytilsmod.core.Config> skytilsConfig = new WeakReference<>(ScoreCalculation.getSkytilsConfig());

    @Nullable
    private static final gg.skytils.skytilsmod.core.Config getSkytilsConfig() {
        try {
            final var companion = gg.skytils.skytilsmod.Skytils.class.getField("Companion").get(null);
            return (gg.skytils.skytilsmod.core.Config) companion.getClass().getMethod("getConfig").invoke(companion);
        } catch (final Throwable error) {
            DarkAddons.modError(error);
            return null;
        }
    }

    @NotNull
    private static final String extractValueFromSkytilsConfig(@NotNull final Function<gg.skytils.skytilsmod.core.Config, String> configCall, @NotNull final String defaultValue) {
        final var config = ScoreCalculation.skytilsConfig.get();
        if (null != config) {
            final var value = configCall.apply(config);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return defaultValue;
    }

    @NotNull
    static final String get300ScoreMessage() {
        return ScoreCalculation.extractValueFromSkytilsConfig(gg.skytils.skytilsmod.core.Config::getMessage300Score, "300 score");
    }

    static final boolean getHasSaid300() {
        return gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation.INSTANCE.getHasSaid300();
    }

    static final void setHasSaid300(final boolean newValue) {
        gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation.INSTANCE.setHasSaid300(newValue);
    }

    @NotNull
    static final String get270ScoreMessage() {
        return ScoreCalculation.extractValueFromSkytilsConfig(gg.skytils.skytilsmod.core.Config::getMessage270Score, "270 score");
    }

    static final boolean getHasSaid270() {
        return gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation.INSTANCE.getHasSaid270();
    }

    static final void setHasSaid270(final boolean newValue) {
        gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation.INSTANCE.setHasSaid270(newValue);
    }

    @NotNull
    static final State<Integer> getTotalScore() {
        return gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation.INSTANCE.getTotalScore();
    }

    @NotNull
    static final BasicState<Integer> getDeaths() {
        return gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation.INSTANCE.getDeaths();
    }
}
