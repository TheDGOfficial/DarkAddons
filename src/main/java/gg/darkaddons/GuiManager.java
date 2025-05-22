package gg.darkaddons;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

final class GuiManager {
    GuiManager() {
        super();
    }

    @NotNull
    private static final HashMap<String, GuiElement> guiElements = new HashMap<>(Utils.calculateHashMapCapacity(16));

    @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened"})
    @NotNull
    private static final ConcurrentLinkedQueue<String> titleQueue = new ConcurrentLinkedQueue<>();

    @Nullable
    private static String title;
    @Nullable
    private static String subtitle;

    private static int titleDisplayTicks;
    private static int subtitleDisplayTicks;

    static final void registerElement(@NotNull final GuiElement element) {
        GuiManager.guiElements.put(element.getName(), element);
    }

    @NotNull
    static final Collection<GuiElement> getElements() {
        return GuiManager.guiElements.values();
    }

    @Nullable
    private static final GuiElement getByName(@Nullable final String name) {
        return GuiManager.guiElements.get(name);
    }

    private static final void handleRender(@NotNull final RenderGameOverlayEvent.Post event) {
        if (DarkAddons.isUsingLabyMod() && !(Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge)) {
            return;
        }

        if (RenderGameOverlayEvent.ElementType.HOTBAR != event.type) {
            return;
        }

        GuiManager.renderHud(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void onRenderGameOverlayEventPost(@NotNull final RenderGameOverlayEvent.Post event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("guimanager_handle_render", event, GuiManager::handleRender);
        } else {
            GuiManager.handleRender(event);
        }
    }

    private static final void handleRenderLabyMod(@NotNull final RenderGameOverlayEvent event) {
        if (!DarkAddons.isUsingLabyMod()) {
            return;
        }

        //noinspection VariableNotUsedInsideIf
        if (null != event.type) {
            return;
        }

        GuiManager.renderHud(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void onRenderGameOverlayEvent(@NotNull final RenderGameOverlayEvent event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("guimanager_handle_render_labymod", event, GuiManager::handleRenderLabyMod);
        } else {
            GuiManager.handleRenderLabyMod(event);
        }
    }

    private static final void renderHud(@NotNull final RenderGameOverlayEvent event) {
        if (DarkAddons.isInLocationEditingGui()) {
            return;
        }

        McProfilerHelper.startSection("dark_addons_render_hud");

        GlState.pushState();

        for (final var element : GuiManager.guiElements.values()) {
            McProfilerHelper.startSection(element.getName());
            element.preRender(false);

            GlStateManager.pushMatrix();
            GlStateManager.translate(element.getScaleX(), element.getScaleY(), 0.0F);
            GlStateManager.scale(element.getScale(), element.getScale(), 0.0F);
            if (Config.isUnsafeMode()) {
                element.render(false);
            } else {
                try {
                    element.render(false);
                } catch (final Throwable t) {
                    DarkAddons.modError(t);
                }
            }
            GlStateManager.popMatrix();
            element.postRender(false);

            McProfilerHelper.endSection();
        }

        McProfilerHelper.startSection("titles");

        GuiManager.renderTitles(event.resolution);

        McProfilerHelper.endSection();

        GlState.popState();

        McProfilerHelper.endSection();
    }

    private static final void handleTick(@NotNull final TickEvent.ClientTickEvent event) {
        if (TickEvent.Phase.START != event.phase) {
            return;
        }

        if (0 < GuiManager.titleDisplayTicks) {
            --GuiManager.titleDisplayTicks;
        } else {
            GuiManager.titleDisplayTicks = 0;
            if (GuiManager.titleQueue.isEmpty()) {
                GuiManager.title = null;
            } else {
                final var iterator = GuiManager.titleQueue.iterator();
                if (iterator.hasNext()) {
                    final var text = iterator.next();
                    GuiManager.createTitle(text, 40, false);

                    iterator.remove();
                } else {
                    GuiManager.title = null;
                }
            }
        }

        if (0 < GuiManager.subtitleDisplayTicks) {
            --GuiManager.subtitleDisplayTicks;
        } else {
            GuiManager.subtitleDisplayTicks = 0;
            GuiManager.subtitle = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void onTick(@NotNull final TickEvent.ClientTickEvent event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("guimanager_handle_tick", event, GuiManager::handleTick);
        } else {
            GuiManager.handleTick(event);
        }
    }

    private static final void renderTitles(@NotNull final ScaledResolution scaledResolution) {
        final var mc = Minecraft.getMinecraft();

        if (null == mc.theWorld || null == mc.thePlayer || !DarkAddons.isInSkyblock()) {
            return;
        }

        final var scaledWidth = scaledResolution.getScaledWidth();
        final var scaledHeight = scaledResolution.getScaledHeight();

        if (null != GuiManager.title) {
            final var stringWidth = mc.fontRendererObj.getStringWidth(GuiManager.title);
            final var scale = 4.0F;

            GuiManager.scale(scaledWidth, scaledHeight, stringWidth, scale);
            mc.fontRendererObj.drawString(
                GuiManager.title,
                -mc.fontRendererObj.getStringWidth(GuiManager.title) >> 1,
                -20.0F,
                0xFF_0000,
                true
            );
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        if (null != GuiManager.subtitle) {
            final var stringWidth = mc.fontRendererObj.getStringWidth(GuiManager.subtitle);
            final var scale = 2.0F;

            GuiManager.scale(scaledWidth, scaledHeight, stringWidth, scale);
            mc.fontRendererObj.drawString(
                GuiManager.subtitle, -mc.fontRendererObj.getStringWidth(GuiManager.subtitle) / 2.0F, -23.0F,
                0xFF_0000, true
            );
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private static final void scale(final int scaledWidth, final int scaledHeight, final int stringWidth, float scale) {
        if (stringWidth * scale > scaledWidth * 0.9F) {
            scale = scaledWidth * 0.9F / stringWidth;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(scaledWidth >> 1, scaledHeight >> 1, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
    }

    @NotNull
    private static final File positionsFile = new File(new File(new File("config"), "darkaddons"), "guipositions.json");

    static final void load() {
        final var element = Utils.parseJsonObjectFromString(GuiManager.read());

        for (final var entry : element.entrySet()) {
            final var name = entry.getKey();
            final var elem = entry.getValue();

            if (elem instanceof final JsonObject obj) {

                final var x = obj.get("x").getAsFloat();
                final var y = obj.get("y").getAsFloat();

                final var scale = obj.get("scale").getAsFloat();

                final var guiElement = GuiManager.getByName(name);

                if (null == guiElement) {
                    // TODO remove the entry since a gui element with that name doesn't exist anymore?
                    DarkAddons.queueWarning("while parsing " + GuiManager.positionsFile.getName() + ": no GuiElement with name: " + name + ", skipping.");
                } else {
                    guiElement.setPosInitial(x, y);
                    guiElement.setScaleInitial(scale);
                }
            } else {
                // TODO automatically reset the file to default if it's corrupt?
                DarkAddons.queueWarning("while parsing " + GuiManager.positionsFile.getName() + ": " + name + " is not a JsonObject (" + elem.getClass().getSimpleName() + "), skipping.");
            }
        }
    }

    static final void save(final boolean force) {
        final var guiElementValues = GuiManager.guiElements.values();

        var dirty = false;

        for (final var guiElement : guiElementValues) {
            // Any GuiElement being dirty requires saving all GuiElement's, as we are writing the whole file again.
            if (guiElement.isDirty()) {
                dirty = true;
                break;
            }
        }

        if (!dirty && !force) {
            // No save needed.
            return;
        }

        final var elements = new JsonObject();

        for (final var guiElement : guiElementValues) {
            final var element = new JsonObject();

            element.addProperty("x", guiElement.getX());
            element.addProperty("y", guiElement.getY());
            element.addProperty("scale", guiElement.getScale());

            elements.add(guiElement.getName(), element);
        }

        GuiManager.write(Utils.toJson(elements));
    }

    private static final void write(@NotNull final String text) {
        Utils.write(GuiManager.positionsFile, text);
    }

    @NotNull
    private static final String read() {
        return Utils.read(GuiManager.positionsFile);
    }

    static final void init() {
        try {
            Files.createDirectories(new File(GuiManager.positionsFile.getParent()).toPath());
            if (!GuiManager.positionsFile.exists()) {
                GuiManager.save(true);
            }
        } catch (final IOException e) {
            DarkAddons.modError(e);
        }
    }

    /*static final void createTitle(@Nullable final String titleText, final int ticks) {
        GuiManager.createTitle(titleText, ticks, true);
    }*/

    static final void createTitle(@Nullable final String titleText, final int ticks, final boolean playsound) {
        GuiManager.createTitle(titleText, ticks, playsound, playsound ? GuiManager.Sound.ORB : GuiManager.Sound.NO_SOUND);
    }

    static final void createTitle(@Nullable final String titleText, final int ticks, final boolean playsound, @NotNull final GuiManager.Sound sound) {
        GuiManager.createTitle(titleText, GuiManager.subtitle, ticks, GuiManager.subtitleDisplayTicks, playsound, sound);
    }

    static final void createTitle(@Nullable final String titleText, @Nullable final String subtitleText, final int ticks, final int subtitleTicks, final boolean playsound, @NotNull final GuiManager.Sound sound) {
        if (playsound && GuiManager.Sound.NO_SOUND != sound) {
            SoundManager.playSound(sound.getMcSound(), 0.5F, 1.0F, false);
        }

        GuiManager.title = titleText;
        GuiManager.subtitle = subtitleText;

        GuiManager.titleDisplayTicks = ticks;

        if (null != GuiManager.subtitle && GuiManager.subtitleDisplayTicks < ticks) {
            GuiManager.subtitleDisplayTicks = subtitleTicks;
        }
    }

    @SuppressWarnings("VariableNotUsedInsideIf")
    static final void queueTitle(@Nullable final String titleText) {
        // using 40 ticks to not have to create and store a Pair<String, Integer> instead of just String on the titleQueue
        if (null == GuiManager.title) {
            GuiManager.createTitle(titleText, 40, false);
        } else if (null == GuiManager.subtitle) { // Add this one as subtitle.
            GuiManager.createTitle(GuiManager.title, titleText, GuiManager.titleDisplayTicks, 40, false, GuiManager.Sound.NO_SOUND);
        } else { // We already have a title and subtitle, queue this one.
            GuiManager.titleQueue.add(titleText);
        }
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    enum Sound {
        ORB("random.orb"),
        PLING("note.pling"),
        LEVEL_UP("random.levelup"),
        ANVIL_LAND("random.anvil_land"),
        NO_SOUND("");

        @NotNull
        private final String mcSound;

        private Sound(@NotNull final String minecraftSound) {
            this.mcSound = minecraftSound;
        }

        @NotNull
        private final String getMcSound() {
            return this.mcSound;
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        @NotNull
        public final String toString() {
            return "Sound{" +
                "mcSound='" + this.mcSound + '\'' +
                '}';
        }
    }
}
