package gg.darkaddons;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MojangUtil {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private MojangUtil() {
        super();

        throw Utils.staticClassException();
    }

    @NotNull
    private static final String API_URL = "https://playerdb.co/api/player/minecraft/";

    @Nullable
    static final UUID getUUIDFromUsername(@NotNull final String username) {
        final var response = Utils.sendWebRequest(MojangUtil.API_URL + username, "application/json", false, 10L);
        if (null != response) {
            final var json = Utils.parseJsonObjectFromString(response);
            final var success = json.get("success").getAsBoolean();
            if (success) {
                return UUID.fromString(json.get("data").getAsJsonObject().get("player").getAsJsonObject().get("id").getAsString());
            }
        }
        return null;
    }
}
