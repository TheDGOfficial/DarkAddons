package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gg.essential.elementa.state.State;
import gg.essential.elementa.state.BasicState;

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

    static final boolean getHasSaid300() {
        return gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation.INSTANCE.getHasSaid300();
    }

    static final void setHasSaid300(final boolean newValue) {
        gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation.INSTANCE.setHasSaid300(newValue);
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
