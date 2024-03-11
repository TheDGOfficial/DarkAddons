package gg.darkaddons;

import gg.essential.api.EssentialAPI;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.constraints.AdditiveConstraint;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.HeightConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RainbowColorConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.elementa.constraints.SiblingConstraint;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.dsl.BasicConstraint;
import gg.essential.elementa.events.UIClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO everything in screen other than this OptionScreen become very small when this menu is opened
@SuppressWarnings("ClassTooDeepInInheritanceTree")
final class OptionsScreen extends WindowScreen {
    @NotNull
    private final UIText title = new UIText("DarkAddons", false);

    @NotNull
    private final OptionsScreen.LambdaHeightConstraint titleScale = new OptionsScreen.LambdaHeightConstraint(() -> this.getWindow().getHeight() / 80.0F);

    OptionsScreen() {
        super(DarkAddons.ELEMENTA_VERSION, true, true, false, EssentialAPI.getGuiUtil().getGuiScale());

        this.initTitle();
        this.initConfig();

        this.initEditLocations();

        this.animate();
    }

    private final void initTitle() {
        this.title.getConstraints().setX(new CenterConstraint());
        this.title.getConstraints().setY(new RelativeConstraint(0.35F));
        this.title.getConstraints().setTextScale(this.titleScale);

        this.getWindow().addChild(this.title);
    }

    private final void initConfig() {
        final var config = new SimpleButton("Config");

        config.getConstraints().setX(new CenterConstraint());
        config.getConstraints().setY(new AdditiveConstraint(new SiblingConstraint(), new RelativeConstraint(0.025F)));

        config.getConstraints().setWidth(new PixelConstraint(400.0F, false, false));
        config.getConstraints().setHeight(new PixelConstraint(40.0F, false, false));

        config.onMouseClickConsumer((@NotNull final UIClickEvent event) -> DarkAddons.openConfigEditor());

        this.getWindow().addChild(config);
    }

    private final void initEditLocations() {
        final var editLocations = new SimpleButton("Edit Locations");

        editLocations.getConstraints().setX(new CenterConstraint());
        editLocations.getConstraints().setY(new AdditiveConstraint(new SiblingConstraint(), new PixelConstraint(4.0F, false, false)));

        editLocations.getConstraints().setWidth(new PixelConstraint(400.0F, false, false));
        editLocations.getConstraints().setHeight(new PixelConstraint(40.0F, false, false));

        editLocations.onMouseClickConsumer((@NotNull final UIClickEvent event) -> DarkAddons.openGui("edit", new OptionsScreen.ElementaEditingScreen()));

        this.getWindow().addChild(editLocations);
    }

    private final void animate() {
        final var animation = this.title.makeAnimation();

        animation.setColorAnimation(Animations.IN_OUT_SIN, 1.0F, new RainbowColorConstraint());
        animation.onCompleteRunnable(this::animate);

        this.title.animateTo(animation);
    }

    @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
    @Override
    public final void setWorldAndResolution(@SuppressWarnings("NullableProblems") @NotNull final Minecraft minecraft, final int w, final int h) {
        this.getWindow().onWindowResize();
        this.title.getConstraints().setTextScale(this.titleScale);

        super.setWorldAndResolution(minecraft, w, h);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final boolean isLocationEditingGui(@Nullable final GuiScreen gui) {
        return gui instanceof OptionsScreen.ElementaEditingScreen;
    }

    @SuppressWarnings("ClassTooDeepInInheritanceTree")
    private static final class ElementaEditingScreen extends WindowScreen {
        private ElementaEditingScreen() {
            super(DarkAddons.ELEMENTA_VERSION);
        }

        @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
        @Override
        public final void initScreen(final int w, final int h) {
            super.initScreen(w, h);

            for (final var guiElement : GuiManager.getElements()) {
                if (guiElement.isEnabled()) {
                    this.getWindow().addChild(new LocationComponent(guiElement));
                }
            }
        }

        @Override
        public final void onScreenClose() {
            super.onScreenClose();

            DarkAddons.saveGuiPositions(false);
        }
    }

    private static final class LambdaHeightConstraint extends BasicConstraint<Float> implements HeightConstraint {
        @NotNull
        private final FloatSupplier floatSupplier;

        private final float getAsFloat() {
            return this.floatSupplier.getAsFloat();
        }

        private LambdaHeightConstraint(@NotNull final FloatSupplier lambda) {
            super(0.0F);

            this.floatSupplier = lambda;
        }

        @Override
        public final float getHeightImpl(@NotNull final UIComponent uiComponent) {
            return this.floatSupplier.getAsFloat();
        }

        @Override
        @NotNull
        public final String toString() {
            return "LambdaHeightConstraint{" +
                "floatSupplier=" + this.floatSupplier +
                '}';
        }
    }

    @Override
    @NotNull
    public final String toString() {
        return "OptionsScreen{" +
            "title=" + this.title.getText() +
            ", titleScale=" + this.titleScale.getAsFloat() +
            '}';
    }
}
