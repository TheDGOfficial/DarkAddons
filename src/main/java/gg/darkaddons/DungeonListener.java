package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;

public final class DungeonListener {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private DungeonListener() {
        super();

        throw Utils.staticClassException();
    }

    @NotNull
    static final HashSet<String> getIncompletePuzzles() {
        return gg.skytils.skytilsmod.listeners.DungeonListener.INSTANCE.getIncompletePuzzles();
    }

    @NotNull
    static final List<DungeonTeammate> getTeam() {
        return gg.skytils.skytilsmod.listeners.DungeonListener.INSTANCE.getTeam().values().stream().map(DungeonListener::mapDungeonTeammate).collect(Collectors.toList());
    }

    @NotNull
    private static final DungeonTeammate mapDungeonTeammate(@NotNull final gg.skytils.skytilsmod.listeners.DungeonListener.DungeonTeammate dungeonTeammate) {
        return new DungeonTeammate(dungeonTeammate.getPlayer(), DungeonListener.mapDungeonClass(dungeonTeammate.getDungeonClass()), dungeonTeammate.getClassLevel());
    }

    @Nullable
    private static final DungeonClass mapDungeonClass(@Nullable final gg.skytils.skytilsmod.utils.DungeonClass dungeonClass) {
        if (null != dungeonClass) {
            final var name = dungeonClass.name();
            if (!"EMPTY".equals(name)) {
                return DungeonClass.valueOf(name);
            }
        }
        return null;
    }

    enum DungeonClass {
        HEALER, MAGE, BERSERK, ARCHER, TANK;

        private DungeonClass() {
        }
    }

    static final class DungeonTeammate {
        @Nullable
        private final EntityPlayer player;
        @Nullable
        private final DungeonClass dungeonClass;
        private final int classLevel;

        private DungeonTeammate(@Nullable final EntityPlayer player, @Nullable final DungeonClass dungeonClass, final int classLevel) {
            this.player = player;
            this.dungeonClass = dungeonClass;
            this.classLevel = classLevel;
        }

        @Nullable
        final EntityPlayer getPlayer() {
           return this.player;
        }

        @Nullable
        final DungeonClass getDungeonClass() {
           return this.dungeonClass;
        }

        final int getClassLevel() {
           return this.classLevel;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(this.player, this.dungeonClass, this.classLevel);
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof final DungeonTeammate other) {
                return Objects.equals(this.player, other.player) && Objects.equals(this.dungeonClass, other.dungeonClass) && Objects.equals(this.classLevel, other.classLevel);
            }
            return false;
        }
    }
}
