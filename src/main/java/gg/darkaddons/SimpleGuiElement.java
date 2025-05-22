package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

abstract class SimpleGuiElement extends GuiElement {
    @NotNull
    private final ArrayList<String> linesToRender;
    private int linesToRenderSize;

    private int width;
    private int height;

    @NotNull
    private final BooleanSupplier isEnabledChecker;

    @NotNull
    private final BooleanSupplier renderPreconditions;

    @NotNull
    private final IntSupplier shadowSelection;

    private boolean demoRenderBypass;

    SimpleGuiElement(@NotNull final String elementName, @NotNull final BooleanSupplier isEnabledCheckerIn, @NotNull final BooleanSupplier renderPreconditionsIn, @NotNull final IntSupplier shadowSelectionIn) {
        this(elementName, isEnabledCheckerIn, renderPreconditionsIn, shadowSelectionIn, 1);
    }

    SimpleGuiElement(@NotNull final String elementName, @NotNull final BooleanSupplier isEnabledCheckerIn, @NotNull final BooleanSupplier renderPreconditionsIn, @NotNull final IntSupplier shadowSelectionIn, final int initialSizeHint) {
        super(elementName);

        this.isEnabledChecker = isEnabledCheckerIn;
        this.renderPreconditions = renderPreconditionsIn;

        this.shadowSelection = shadowSelectionIn;
        this.linesToRender = new ArrayList<>(initialSizeHint);
    }

    abstract void buildHudLines(@NotNull final Collection<String> lines);

    @NotNull
    private final String findLongestLine() {
        var longestLine = "";
        var longestLength = 0;

        for (final var line : this.linesToRender) {
            final var len = line.length();

            if (len > longestLength) {
                longestLine = line;
                longestLength = len;
            }
        }

        return longestLine;
    }

    private final void updateWidthHeightSize() {
        this.linesToRenderSize = this.linesToRender.size();

        this.width = GuiElement.getTextWidth(this.findLongestLine());
        this.height = GuiElement.getFontHeight() * this.linesToRenderSize;
    }

    void clear() {
        this.linesToRender.clear();
        this.updateWidthHeightSize();
    }

    final boolean isDemoRenderBypass() {
        return this.demoRenderBypass;
    }

    final boolean isEmpty() {
        return this.linesToRender.isEmpty();
    }

    void update() {
        this.linesToRender.clear();
        this.buildHudLines(this.linesToRender);

        this.updateWidthHeightSize();
    }

    @Override
    final void preRender(final boolean demo) {
        super.preRender(demo);

        if (demo && 0 == this.linesToRenderSize) {
            this.demoRenderBypass = true;
            this.update();
        }
    }

    @Override
    final void postRender(final boolean demo) {
        super.postRender(demo);

        if (demo && this.demoRenderBypass) {
            this.demoRenderBypass = false;
            this.clear();
        }
    }

    @Override
    final void render(final boolean demo) {
        if (demo || this.isEnabled() && !DarkAddons.isInLocationEditingGui() && this.renderPreconditions.getAsBoolean()) {
            final var leftAlign = this.shouldLeftAlign();
            final var xPos = leftAlign ? 0.0F : this.getWidth(demo);

            final var shadow = switch (this.shadowSelection.getAsInt()) {
                case 1 -> SmartFontRenderer.TextShadow.NORMAL;
                case 2 -> SmartFontRenderer.TextShadow.OUTLINE;
                default -> SmartFontRenderer.TextShadow.NONE;
            };

            final var fontHeight = GuiElement.getFontHeight();

            final var length = this.linesToRenderSize;

            for (var i = 0; i < length; ++i) {
                GuiElement.drawString(
                    this.linesToRender.get(i),
                    xPos,
                    i * fontHeight,
                    leftAlign,
                    shadow
                );
            }
        }
    }

    @Override
    final boolean isEnabled() {
        return this.isEnabledChecker.getAsBoolean();
    }

    @Override
    final int getHeight() {
        return this.height;
    }

    @Override
    final int getWidth(final boolean demo) {
        return this.width;
    }
}
