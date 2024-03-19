package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Mixin(value = ItemModelGenerator.class, priority = 1_001)
final class MixinItemModelGenerator {
    private MixinItemModelGenerator() {
        super();
    }

    /**
     * Fixes item modeling by overriding item model generation.
     *
     * @author Pepper_Bell, asbyth, TheDGOfficial
     * @reason Port of <a href="https://github.com/Sk1erLLC/ModelFix">ModelFix</a> (which is a port of <a href="https://www.curseforge.com/minecraft/mc-mods/item-model-fix">item-model-fix</a> to work in 1.8.9) to use Mixin instead of transformer
     */
    @Overwrite
    @NotNull
    private final List<BlockPart> func_178397_a(@NotNull final TextureAtlasSprite textureAtlasSprite, @NotNull final String string, final int i) {
        return MixinItemModelGenerator.getBlockParts(textureAtlasSprite, string, i);
    }

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @Unique
    @NotNull
    private static final ArrayList<BlockPart> getBlockParts(@NotNull final TextureAtlasSprite sprite, @NotNull final String key, final int layer) {
        final var elements = new ArrayList<BlockPart>(100);
        final var width = sprite.getIconWidth();
        final var height = sprite.getIconHeight();

        final float xRatio = width >> 4;
        final float yRatio = height >> 4;

        var size = 0;

        final var frameCountLen = sprite.getFrameCount();
        for (var frameCount = 0; frameCount < frameCountLen; ++frameCount) {
            final var textureData = sprite.getFrameTextureData(frameCount)[0];

            size = MixinItemModelGenerator.addElements(key, layer, height, width, textureData, size, elements, xRatio, yRatio);
        }

        return elements;
    }

    @Unique
    private static final int addElements(@NotNull final String key, final int layer, final int height, final int width, @NotNull final int[] textureData, int size, @NotNull final ArrayList<BlockPart> elements, final float xRatio, final float yRatio) {
        for (var y = 0; y < height; ++y) {
            for (var x = 0; x < width; ++x) {
                final var previous = 0 > x - 1 || MixinItemModelGenerator.isTransparent(textureData, x - 1, y, width, height);
                final var current = MixinItemModelGenerator.isTransparent(textureData, x, y, width, height);

                if (!current) {
                    ++size;
                }

                if (!previous && current) {
                    elements.add(MixinItemModelGenerator.horizontalElement(x, y, size, height, xRatio, yRatio, key, layer));
                    size = 0;
                }
            }

            if (0 != size) {
                elements.add(MixinItemModelGenerator.horizontalElement(width, y, size, height, xRatio, yRatio, key, layer));
                size = 0;
            }
        }

        for (var x = 0; x < width; ++x) {
            for (var y = 0; y < height; ++y) {
                final var previous = 0 > y - 1 || MixinItemModelGenerator.isTransparent(textureData, x, y - 1, width, height);
                final var current = MixinItemModelGenerator.isTransparent(textureData, x, y, width, height);

                if (!current) {
                    ++size;
                }

                if (!previous && current) {
                    elements.add(MixinItemModelGenerator.verticalElement(x, y, size, height, xRatio, yRatio, key, layer));
                    size = 0;
                }
            }

            if (0 != size) {
                elements.add(MixinItemModelGenerator.verticalElement(x, height, size, height, xRatio, yRatio, key, layer));
                size = 0;
            }
        }

        return size;
    }

    @Unique
    private static final BlockPart verticalElement(final int x, final int y, final int size, final int height, final float xRatio, final float yRatio, @NotNull final String key, final int layer) {
        final var map = new EnumMap<EnumFacing, BlockPartFace>(EnumFacing.class);
        map.put(EnumFacing.UP, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{x / xRatio, (y - size) / yRatio, (x + 1) / xRatio, (y - size + 1) / yRatio}, 0)));
        map.put(EnumFacing.DOWN, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{x / xRatio, (y - 1) / yRatio, (x + 1) / xRatio, y / yRatio}, 0)));
        return MixinItemModelGenerator.newBlockPart(x, y, size, height, xRatio, yRatio, map);
    }

    @Unique
    private static final BlockPart horizontalElement(final int x, final int y, final int size, final int height, final float xRatio, final float yRatio, @NotNull final String key, final int layer) {
        final var map = new EnumMap<EnumFacing, BlockPartFace>(EnumFacing.class);
        map.put(EnumFacing.NORTH, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{x / xRatio, y / yRatio, (x - size) / xRatio, (y + 1) / yRatio}, 0)));
        map.put(EnumFacing.SOUTH, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - size) / xRatio, y / yRatio, x / xRatio, (y + 1) / yRatio}, 0)));
        map.put(EnumFacing.WEST, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - size) / xRatio, y / yRatio, (x - size + 1) / xRatio, (y + 1) / yRatio}, 0)));
        map.put(EnumFacing.EAST, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - 1) / xRatio, y / yRatio, x / xRatio, (y + 1) / yRatio}, 0)));
        return MixinItemModelGenerator.newBlockPart(x, y, size, height, xRatio, yRatio, map);
    }

    @Unique
    @NotNull
    private static final BlockPart newBlockPart(final int x, final int y, final int size, final int height, final float xRatio, final float yRatio, @NotNull final EnumMap<EnumFacing, BlockPartFace> map) {
        return new BlockPart(new Vector3f((x - size) / xRatio, (height - (y + 1)) / yRatio, 7.5f), new Vector3f(x / xRatio, (height - y) / yRatio, 8.5F), map, null, true);
    }

    @Unique
    private static final boolean isTransparent(@NotNull final int[] textureData, final int x, final int y, final int width, final int height) {
        return 0 > x || 0 > y || x >= width || y >= height || 0 == (textureData[y * width + x] >> 24 & 255);
    }
}
