package gg.darkaddons;

import gg.essential.elementa.UIComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.constraints.AdditiveConstraint;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.ConstantColorConstraint;
import gg.essential.elementa.constraints.MousePositionConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.elementa.constraints.SubtractiveConstraint;
import gg.essential.elementa.events.UIClickEvent;
import gg.essential.elementa.events.UIScrollEvent;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UResolution;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

final class LocationComponent extends UIComponent {
    @NotNull
    private final GuiElement element;
    @NotNull
    private final UIBlock background = new UIBlock();

    LocationComponent(final GuiElement guiElement) {
        super();

        this.element = guiElement;

        this.getConstraints().setX(new RelativeConstraint(guiElement.getX()));
        this.getConstraints().setY(new RelativeConstraint(guiElement.getY()));

        this.getConstraints().setWidth(new PixelConstraint(guiElement.getScaledWidth(), false, false));
        this.getConstraints().setHeight(new PixelConstraint(guiElement.getScaledHeight(), false, false));

        this.onMouseClickConsumer((final UIClickEvent event) -> {
            this.getConstraints().setX(new SubtractiveConstraint(new MousePositionConstraint(), new PixelConstraint(event.getRelativeX(), false, false)));
            this.getConstraints().setY(new SubtractiveConstraint(new MousePositionConstraint(), new PixelConstraint(event.getRelativeY(), false, false)));
        });

        this.onMouseReleaseRunnable(() -> {
            final var scaleX = this.getLeft() / UResolution.getScaledWidth();
            final var scaleY = this.getTop() / UResolution.getScaledHeight();

            this.getConstraints().setX(new RelativeConstraint(scaleX));
            this.getConstraints().setY(new RelativeConstraint(scaleY));
            guiElement.setPos(scaleX, scaleY);
        });

        this.onMouseScrollConsumer((@NotNull final UIScrollEvent event) -> {
            guiElement.setScale((float) Math.max(guiElement.getScale() + event.getDelta(), 0.05D));
            this.getConstraints().setWidth(new PixelConstraint(guiElement.getScaledWidth(), false, false));
            this.getConstraints().setHeight(new PixelConstraint(guiElement.getScaledHeight(), false, false));
            event.stopImmediatePropagation();
        });

        this.background.getConstraints().setColor(new ConstantColorConstraint(LocationComponent.withAlpha(Color.WHITE, 40)));

        this.background.getConstraints().setWidth(new AdditiveConstraint(new RelativeConstraint(1.0F), new PixelConstraint(8.0F, false, false)));
        this.background.getConstraints().setHeight(new AdditiveConstraint(new RelativeConstraint(1.0F), new PixelConstraint(8.0F, false, false)));

        this.background.setX(new CenterConstraint());
        this.background.setY(new CenterConstraint());

        this.background.onMouseEnterRunnable(() -> this.background.setColor(LocationComponent.withAlpha(Color.WHITE, 100)));
        this.background.onMouseLeaveRunnable(() -> this.background.setColor(LocationComponent.withAlpha(Color.WHITE, 40)));

        this.addChild(this.background);
    }

    @NotNull
    private static final Color withAlpha(@NotNull final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    @Override
    public final void draw(@NotNull final UMatrixStack matrixStack) {
        this.beforeDraw(matrixStack);
        super.draw(matrixStack);

        matrixStack.push();
        matrixStack.translate(this.getLeft(), this.getTop(), 0.0F);

        final var scale = this.element.getScale();
        matrixStack.scale(scale, scale, 1.0F);
        matrixStack.runWithGlobalState(() -> this.element.render(true));
        matrixStack.pop();
    }

    @Override
    @NotNull
    public final String toString() {
        return "LocationComponent{" +
            "element=" + this.element +
            ", background=" + this.background.getComponentName() +
            '}';
    }
}
