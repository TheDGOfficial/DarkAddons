package gg.darkaddons;

import gg.essential.universal.UResolution;
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer;
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors;
import org.jetbrains.annotations.NotNull;

abstract class GuiElement {
    @NotNull
    private final String name;
    private float scale;
    private float x;
    private float y;
    private boolean dirty;

    GuiElement(@NotNull final String elementName) {
        this(elementName, 0, 0);
    }

    private GuiElement(@NotNull final String elementName, final int initialX, final int initialY) {
        this(elementName, 1.0F, GuiElement.scaleX(initialX), GuiElement.scaleY(initialY));
    }

    private GuiElement(@NotNull final String elementName, final float initialScale, final float initialX, final float initialY) {
        super();

        this.name = elementName;
        this.scale = initialScale;

        this.x = initialX;
        this.y = initialY;
    }

    @NotNull
    final String getName() {
        return this.name;
    }

    final float getScale() {
        return this.scale;
    }

    final void setScale(final float newScale) {
        if (!Utils.compareFloatExact(this.scale, newScale)) {
            this.scale = newScale;
            this.dirty = true;
        }
    }

    final void setScaleInitial(final float newScale) {
        if (!Utils.compareFloatExact(1.0F, this.scale)) {
            throw new IllegalStateException("setScaleInitial() must be called once after init");
        }

        this.scale = newScale;
    }

    final float getScaledHeight() {
        return this.getHeight() * this.scale;
    }

    final float getScaledWidth() {
        return this.getWidth(true) * this.scale;
    }

    private static final float scaleX(final int xCoord) {
        return xCoord / (float) UResolution.getScaledWidth();
    }

    private static final float scaleY(final int yCoord) {
        return yCoord / (float) UResolution.getScaledHeight();
    }

    void preRender(final boolean demo) {
        // do nothing, subclasses can implement custom behavior
    }

    void postRender(final boolean demo) {
        // do nothing, subclasses can implement custom behavior
    }

    abstract void render(final boolean demo);

    static final int getFontHeight() {
        return ScreenRenderer.Companion.getFontRenderer().FONT_HEIGHT;
    }

    static final int getTextWidth(@NotNull final String text) {
        return ScreenRenderer.Companion.getFontRenderer().getStringWidth(text);
    }

    final boolean shouldLeftAlign() {
        return this.getScaleX() < UResolution.getScaledWidth() / 2.0F;
    }

    static final void drawString(@NotNull final String text, final float xPos, final float yPos, final boolean leftAligned) {
        GuiElement.drawString(text, xPos, yPos, CommonColors.Companion.getWHITE(), leftAligned);
    }

    static final void drawString(@NotNull final String text, final float xPos, final float yPos, final boolean leftAligned, @NotNull final SmartFontRenderer.TextShadow shadow) {
        GuiElement.drawString(text, xPos, yPos, CommonColors.Companion.getWHITE(), leftAligned, shadow);
    }

    static final void drawString(@NotNull final String text, final float xPos, final float yPos, @NotNull final CommonColors color, final boolean leftAligned) {
        GuiElement.drawString(text, xPos, yPos, color, leftAligned, SmartFontRenderer.TextShadow.NONE);
    }

    static final void drawString(@NotNull final String text, final float xPos, final float yPos, @NotNull final CommonColors color, final boolean leftAligned, @NotNull final SmartFontRenderer.TextShadow shadow) {
        ScreenRenderer.Companion.getFontRenderer().drawString(
            text,
            xPos,
            yPos,
            color,
            leftAligned ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT,
            shadow
        );
    }

    abstract boolean isEnabled();

    abstract int getHeight();

    abstract int getWidth(final boolean demo);

    final void setPos(final float newX, final float newY) {
        if (!Utils.compareFloatExact(this.x, newX)) {
            this.x = newX;
            this.dirty = true;
        }

        if (!Utils.compareFloatExact(this.y, newY)) {
            this.y = newY;
            this.dirty = true;
        }
    }

    final void setPosInitial(final float newX, final float newY) {
        if (!Utils.compareFloatExact(0.0F, this.x) || !Utils.compareFloatExact(0.0F, this.y)) {
            throw new IllegalStateException("setPosInitial() must be called once after init");
        }

        this.x = newX;
        this.y = newY;
    }

    final boolean isDirty() {
        return this.dirty;
    }

    final float getScaleX() {
        return UResolution.getScaledWidth() * this.x;
    }

    final float getScaleY() {
        return UResolution.getScaledHeight() * this.y;
    }

    final float getX() {
        return this.x;
    }

    final float getY() {
        return this.y;
    }

    @Override
    public final String toString() {
        return "GuiElement{" +
            "name='" + this.name + '\'' +
            ", scale=" + this.scale +
            ", x=" + this.x +
            ", y=" + this.y +
            ", dirty=" + this.dirty +
            '}';
    }
}
