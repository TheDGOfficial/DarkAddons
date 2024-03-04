package gg.darkaddons;

import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.ConstantColorConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.events.UIClickEvent;
import gg.essential.universal.USound;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
final class SimpleButton extends UIBlock {
    @NotNull
    private final UIText uiText;

    SimpleButton(@NotNull final String text) {
        super(new Color(0, 0, 0, 80));

        this.uiText = new UIText(text);

        this.uiText.getConstraints().setX(new CenterConstraint());
        this.uiText.getConstraints().setY(new CenterConstraint());
        this.uiText.getConstraints().setColor(new ConstantColorConstraint(new Color(14_737_632)));

        this.addChild(this.uiText);

        this.getConstraints().setWidth(new PixelConstraint(this.uiText.getWidth() + 40.0F, false, false));
        this.getConstraints().setHeight(new PixelConstraint(this.uiText.getHeight() + 10.0F, false, false));

        this.onMouseEnterRunnable(() -> {
            final var anim = this.makeAnimation();
            anim.setColorAnimation(Animations.OUT_EXP, 0.5F, new ConstantColorConstraint(new Color(255, 255, 255, 80)), 0.0F);

            this.animateTo(anim);

            this.uiText.getConstraints().setColor(new ConstantColorConstraint(new Color(16_777_120)));
        });

        this.onMouseLeaveRunnable(() -> {
            final var anim = this.makeAnimation();
            anim.setColorAnimation(Animations.OUT_EXP, 0.5F, new ConstantColorConstraint(new Color(0, 0, 0, 80)));

            this.animateTo(anim);

            this.uiText.getConstraints().setColor(new ConstantColorConstraint(new Color(14_737_632)));
        });

        this.onMouseClickConsumer((@NotNull final UIClickEvent event) -> {
            if (0 == event.getMouseButton()) {
                USound.INSTANCE.playButtonPress();
            }
        });
    }

    @Override
    @NotNull
    public final String toString() {
        return "SimpleButton{" +
                "uiText=" + this.uiText.getText() +
                ", parent=" + this.parent.getComponentName() +
                '}';
    }
}
