package gg.darkaddons;

import gg.essential.elementa.font.DefaultFonts;
import gg.essential.elementa.font.ElementaFonts;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

final class RenderUtil {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private RenderUtil() {
        super();

        throw Utils.staticClassException();
    }

    static final float getPartialTicks() {
        return ((AccessorMinecraft) Minecraft.getMinecraft()).getTimer().renderPartialTicks;
    }

    private static final double interpolate(final double currentValue, final double lastValue, final float multiplier) {
        return lastValue + (currentValue - lastValue) * multiplier;
    }

    /*private static final void drawLabel(@NotNull final Vec3 pos, @NotNull final String text, @NotNull final Color color, final float partialTicks, @NotNull final UMatrixStack matrixStack, final boolean shadow, final float scale) {
        RenderUtil.drawNametag(pos.xCoord, pos.yCoord, pos.zCoord, text, color, partialTicks, matrixStack, shadow, scale, false);
    }*/

    static final void drawOutlinedBoundingBox(@Nullable final AxisAlignedBB aabb, @NotNull final Color color, final float width, final float partialTicks) {
        final var render = Minecraft.getMinecraft().getRenderViewEntity();
        final var realX = RenderUtil.interpolate(render.posX, render.lastTickPosX, partialTicks);
        final var realY = RenderUtil.interpolate(render.posY, render.lastTickPosY, partialTicks);
        final var realZ = RenderUtil.interpolate(render.posZ, render.lastTickPosZ, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-realX, -realY, -realZ);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glLineWidth(width);
        RenderGlobal.drawOutlinedBoundingBox(aabb, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        GlStateManager.translate(realX, realY, realZ);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    @SuppressWarnings("deprecation")
    static final void drawNametag(final double x, final double y, final double z, @NotNull final String str, @NotNull final Color color, final float partialTicks, @NotNull final UMatrixStack matrixStack, final boolean shadow, final float scale, final boolean background) {
        final var mc = Minecraft.getMinecraft();

        final var player = mc.thePlayer;
        final var x1 = x - player.lastTickPosX + (x - player.posX - (x - player.lastTickPosX)) * partialTicks;
        final var y1 = y - player.lastTickPosY + (y - player.posY - (y - player.lastTickPosY)) * partialTicks;
        final var z1 = z - player.lastTickPosZ + (z - player.posZ - (z - player.lastTickPosZ)) * partialTicks;
        final var width = mc.fontRendererObj.getStringWidth(str) >> 1;
        matrixStack.push();
        matrixStack.translate(x1, y1, z1);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        matrixStack.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        matrixStack.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        matrixStack.scale(-0.026_666_668_8, -0.026_666_668_8, -0.026_666_668_8);
        UGraphics.disableLighting();
        UGraphics.depthMask(false);
        UGraphics.enableBlend();
        UGraphics.tryBlendFuncSeparate(770, 771, 1, 0);
        if (background) {
            RenderUtil.drawBackground(matrixStack, width);
        }
        GlStateManager.enableTexture2D();
        DefaultFonts.getVANILLA_FONT_RENDERER().drawString(
            matrixStack,
            str,
            color,
            -width,
            ElementaFonts.getMINECRAFT().getBelowLineHeight() * scale,
            width * 2.0F,
            scale,
            shadow,
            null
        );
        UGraphics.depthMask(true);
        matrixStack.pop();
    }

    @SuppressWarnings("deprecation")
    private static final void drawBackground(@NotNull final UMatrixStack matrixStack, final int width) {
        final var worldRenderer = UGraphics.getFromTessellator();
        worldRenderer.beginWithDefaultShader(UGraphics.DrawMode.QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(matrixStack, -width - 1.0D, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldRenderer.pos(matrixStack, -width - 1.0D, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldRenderer.pos(matrixStack, width + 1.0D, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldRenderer.pos(matrixStack, width + 1.0D, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldRenderer.drawDirect();
    }
}
