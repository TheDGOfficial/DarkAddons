package gg.darkaddons;

import net.minecraft.client.Minecraft;

import net.minecraft.client.settings.KeyBinding;

import org.jetbrains.annotations.NotNull;

final class SafePickobulus {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private SafePickobulus() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean shouldAllowKeyPress(@NotNull final KeyBinding keyBinding) {
        if (Config.isSafePickobulus() && (SkyblockIsland.TheGarden.isInIsland() || SkyblockIsland.PrivateIsland.isInIsland())) {
            final var mc = Minecraft.getMinecraft();
            final var settings = mc.gameSettings;

            if (settings.keyBindUseItem == keyBinding) {
                final var held = ItemUtils.getHeldItemStack(mc);
                if (null != held) {
                    for (final var line : ItemUtils.getItemLore(held)) {
                        final var cleanLine = Utils.removeControlCodes(line).trim();

                        if (cleanLine.contains("Ability: ") && cleanLine.endsWith("RIGHT CLICK")) {
                            if (cleanLine.contains("Pickobulus")) {
                                return false;
                            }

                            break;
                        }
                    }
                }
            }
        }

        return true;
    }
}
