package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer;

final class SmartFontRenderer {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private SmartFontRenderer() {
        super();

        throw Utils.staticClassException();
    }

    static final void drawString(@NotNull final String text, final float xPos, final float yPos, @NotNull final SmartFontRenderer.CommonColors color, final boolean leftAligned, @NotNull final SmartFontRenderer.TextShadow shadow) {
        ScreenRenderer.Companion.getFontRenderer().drawString(text, xPos, yPos, color.color, leftAligned ? gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment.LEFT_RIGHT : gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment.RIGHT_LEFT, shadow.shadow);
    }

    enum TextShadow {
        NORMAL(gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow.NORMAL), OUTLINE(gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow.OUTLINE), NONE(gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow.NONE);

        @NotNull
        private final gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow shadow;

        private TextShadow(@NotNull final gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow shadow) {
            this.shadow = shadow;
        }
    }

    enum CommonColors {
        WHITE(gg.skytils.skytilsmod.utils.graphics.colors.CommonColors.Companion.getWHITE()), RED(gg.skytils.skytilsmod.utils.graphics.colors.CommonColors.Companion.getRED()), YELLOW(gg.skytils.skytilsmod.utils.graphics.colors.CommonColors.Companion.getYELLOW()), GREEN(gg.skytils.skytilsmod.utils.graphics.colors.CommonColors.Companion.getGREEN()), RAINBOW(gg.skytils.skytilsmod.utils.graphics.colors.CommonColors.Companion.getRAINBOW());

        @NotNull
        private final gg.skytils.skytilsmod.utils.graphics.colors.CommonColors color;

        private CommonColors(@NotNull final gg.skytils.skytilsmod.utils.graphics.colors.CommonColors color) {
            this.color = color;
        }
    }
}
