package gg.darkaddons;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import gg.skytils.skytilsmod.core.API;
import gg.skytils.skytilsmod.utils.UtilsKt;

import gg.skytils.hypixel.types.skyblock.Member;
import gg.skytils.hypixel.types.skyblock.Profile;

import gg.skytils.hypixel.types.skyblock.DungeonsData;
import gg.skytils.hypixel.types.skyblock.DungeonData;
import gg.skytils.hypixel.types.skyblock.DungeonModeData;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

final class HypixelAPI {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private HypixelAPI() {
        super();

        throw Utils.staticClassException();
    }

    /**
     * Caches profiles up to 3 seconds to prevent API spam.
     */
    @NotNull
    private static final Cache<UUID, Profile> profileCache = CacheBuilder.newBuilder().expireAfterAccess(3L, TimeUnit.SECONDS).build();

    static final DungeonAPIResponse getDungeonStats(@NotNull final UUID uuid) {
        final var profile = HypixelAPI.getProfile(uuid);

        if (null == profile) {
            return new DungeonAPIResponse(DungeonAPIResponse.FailReason.NO_API_DATA);
        }

        final var member = HypixelAPI.getMember(profile, uuid);

        if (null == member) {
             return new DungeonAPIResponse(DungeonAPIResponse.FailReason.NO_API_DATA);
        }

        final var stats = member.getDungeons();

        if (null == stats) {
            return new DungeonAPIResponse(DungeonAPIResponse.FailReason.NO_DUNGEON_DATA);
        }

        final var data = stats.getDungeon_types().get("catacombs");
        final double cataXp;

        if (null == data || 0.0D == (cataXp = data.getExperience())) {
            return new DungeonAPIResponse(DungeonAPIResponse.FailReason.NO_DUNGEON_DATA);
        }

        return new DungeonAPIResponse(cataXp, stats, data);
    }

    static final class DungeonAPIResponse {
        private final boolean successful;
        @Nullable
        private final DungeonAPIResponse.FailReason failReason;

        private final double cataXp;

        @Nullable
        private final DungeonsData stats;

        @Nullable
        private final DungeonData data;

        private DungeonAPIResponse(@NotNull final DungeonAPIResponse.FailReason failReason) {
            this.successful = false;
            this.failReason = failReason;
            this.cataXp = -1.0D;
            this.stats = null;
            this.data = null;
        }

        private DungeonAPIResponse(final double cataXp, @NotNull final DungeonsData stats, @NotNull final DungeonData data) {
            this.successful = true;
            this.failReason = null;
            this.cataXp = cataXp;
            this.stats = stats;
            this.data = data;
        }

        final boolean isSuccessful() {
            return this.successful;
        }

        @NotNull
        final DungeonAPIResponse.FailReason getFailReason() {
            if (this.isSuccessful()) {
                throw new IllegalStateException("request is successful");
            }
            return this.failReason;
        }

        final double getCataXp() {
            if (!this.isSuccessful()) {
                throw new IllegalStateException("request failed");
            }
            return this.cataXp;
        }

        final double getClassXpForClass(@NotNull final DungeonListener.DungeonClass dungeonClass) {
            if (!this.isSuccessful()) {
                throw new IllegalStateException("request failed");
            }
            return Utils.toPrimitive(this.stats.getPlayer_classes().get(dungeonClass.name().toLowerCase(Locale.ROOT)).getExperience());
        }

        final long getMasterTierCompletionsForFloor(@NotNull final int floorNumber) {
            if (!this.isSuccessful()) {
                throw new IllegalStateException("request failed");
            }
            return (long) HypixelAPI.getMasterCompletions(this.data, floorNumber);
        }

        enum FailReason {
            NO_API_DATA, NO_DUNGEON_DATA;
        }
    }

    @Nullable
    static final String getFormattedRankAndName(@NotNull final UUID uuid) {
        final var player = API.INSTANCE.getPlayerSync(uuid);

        if (null == player) {
            return null;
        }

        return UtilsKt.getFormattedName(player);
    }

    @Nullable
    private static final Member getMember(@NotNull final Profile profile, @NotNull final UUID uuid) {
        return profile.getMembers().get(StringUtils.remove(uuid.toString(), '-'));
    }

    @Nullable
    private static final Profile getProfile(@NotNull final UUID uuid) {
        var profile = HypixelAPI.profileCache.getIfPresent(uuid);

        if (null == profile) {
            profile = API.INSTANCE.getSelectedSkyblockProfileSync(uuid);

            if (null == profile) {
                return null;
            }

            HypixelAPI.profileCache.put(uuid, profile);
        }

        return profile;
    }

    @NotNull
    private static final Map<String, Double> getCompletionsCatching(final DungeonModeData dungeon) {
        try {
            return dungeon.getTier_completions();
        } catch (final NumberFormatException nfe) {
            @SuppressWarnings("TypeMayBeWeakened") final var map = new HashMap<String, Double>(Utils.calculateHashMapCapacity(7));

            for (var i = 0; 7 > i; ++i) {
                map.put(Integer.toString(i), 0.0D);
            }

            return map;
        }
    }

    private static final double getMasterCompletions(@NotNull final DungeonData data, @NotNull final int floorNumber) {
        final var master = data.getMaster();
        final var masterCompletions = null == master ? null : HypixelAPI.getCompletionsCatching(master);

        return null == masterCompletions ? 0.0D : Utils.toPrimitive(masterCompletions.get(Integer.toString(floorNumber)));
    }
}
