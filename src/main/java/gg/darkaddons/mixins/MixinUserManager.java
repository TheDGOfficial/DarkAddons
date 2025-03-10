package gg.darkaddons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

@Pseudo
@Mixin(targets = "net.labymod.user.UserManager$2", remap = false, priority = 1_001)
final class MixinUserManager {
    private MixinUserManager() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "accept", remap = false, at = @At("RETURN"))
    private final void afterLoadWhitelist$darkaddons(@NotNull final Integer accepted, @NotNull final CallbackInfo ci) {
        try {
            final var outerClassField = this.getClass().getDeclaredField("this$0");
            outerClassField.setAccessible(true);

            final var userManager = outerClassField.get(this);
            outerClassField.setAccessible(false);

            final var whitelistedUsersField = userManager.getClass().getDeclaredField("whitelistedUsers");
            whitelistedUsersField.setAccessible(true);

            final var whitelistedUsers = (ArrayList<Long>) whitelistedUsersField.get(userManager);
            whitelistedUsers.trimToSize();

            whitelistedUsersField.setAccessible(false);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Reflective operation inside mixin MixinUserManager failed", e);
        }
    }
}
