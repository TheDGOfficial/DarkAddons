package gg.darkaddons;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;

final class GlState {
    private static final boolean NEW_BLEND;
    private static final ArrayDeque<GlState> STACK = new ArrayDeque<>(1);

    static {
        final var context = GLContext.getCapabilities();
        NEW_BLEND = context.OpenGL14 || context.GL_EXT_blend_func_separate;
    }

    public static final void pushState() {
        final var state = new GlState();
        state.capture();
        GlState.STACK.addLast(state);
    }

    public static final void popState() {
        final var state = GlState.STACK.removeLast();
        state.restore();
    }

    private boolean lightingState;
    private boolean blendState;
    private int blendSrc;
    private int blendDst;
    private int blendAlphaSrc = 1;
    private int blendAlphaDst;
    private boolean alphaState;
    private boolean depthState;
    private final FloatBuffer colorState = GLAllocation.createDirectByteBuffer(64).asFloatBuffer();

    private GlState() {
        super();
    }

    private final void capture() {
        this.lightingState = GL11.glIsEnabled(GL11.GL_LIGHTING);
        this.blendState = GL11.glIsEnabled(GL11.GL_BLEND);
        this.blendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        this.blendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        this.alphaState = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        this.depthState = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);

        if (GlState.NEW_BLEND) {
            this.blendSrc = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
            this.blendDst = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
            this.blendAlphaSrc = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
            this.blendAlphaDst = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        }

        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, this.colorState);
    }

    private final void restore() {
        if (this.depthState) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }

        if (this.blendState) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }

        if (this.alphaState) {
            GlStateManager.enableAlpha();
        } else {
            GlStateManager.disableAlpha();
        }

        if (this.lightingState) {
            GlStateManager.enableLighting();
        } else {
            GlStateManager.disableLighting();
        }

        GlStateManager.tryBlendFuncSeparate(this.blendSrc, this.blendDst, this.blendAlphaSrc, this.blendAlphaDst);
        GlStateManager.color(this.colorState.get(0), this.colorState.get(1), this.colorState.get(2), this.colorState.get(3));
    }

    @Override
    public final String toString() {
        return "GlState{" +
            "lightingState=" + this.lightingState +
            ", blendState=" + this.blendState +
            ", blendSrc=" + this.blendSrc +
            ", blendDst=" + this.blendDst +
            ", blendAlphaSrc=" + this.blendAlphaSrc +
            ", blendAlphaDst=" + this.blendAlphaDst +
            ", alphaState=" + this.alphaState +
            ", depthState=" + this.depthState +
            ", colorState=" + this.colorState +
            '}';
    }
}
