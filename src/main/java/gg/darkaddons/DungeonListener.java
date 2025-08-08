package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;

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
    static final List<DungeonListener.DungeonTeammate> getTeam() {
        return gg.skytils.skytilsmod.listeners.DungeonListener.INSTANCE.getTeam().values().stream().map(DungeonListener::mapDungeonTeammate).collect(Collectors.toList());
    }

    @Nullable
    static final DungeonListener.DungeonTeammate getSelfDungeonTeammate() {
        final var self = Minecraft.getMinecraft().thePlayer;
        for (final var teammate : DungeonListener.getTeam()) {
            //noinspection ObjectEquality
            if (self == teammate.getPlayer()) {
                return teammate;
            }
        }
        return null;
    }

    @Nullable
    static final DungeonListener.DungeonClass getSelfDungeonClass() {
        final var self = DungeonListener.getSelfDungeonTeammate();
        if (null != self) {
            final var dungeonClass = self.getDungeonClass();
            if (null != dungeonClass) {
                return dungeonClass;
            }
        }
        return null;
    }

    @NotNull
    private static final DungeonListener.DungeonTeammate mapDungeonTeammate(@NotNull final gg.skytils.skytilsmod.listeners.DungeonListener.DungeonTeammate dungeonTeammate) {
        return new DungeonListener.DungeonTeammate(dungeonTeammate.getPlayer(), DungeonListener.mapDungeonClass(dungeonTeammate.getDungeonClass()), dungeonTeammate.getClassLevel());
    }

    @Nullable
    private static final DungeonListener.DungeonClass mapDungeonClass(@Nullable final gg.skytils.skytilsmod.utils.DungeonClass dungeonClass) {
        if (null != dungeonClass) {
            final var name = dungeonClass.name();
            if (!"EMPTY".equals(name)) {
                return DungeonListener.DungeonClass.valueOf(name);
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
        private final DungeonListener.DungeonClass dungeonClass;
        private final int classLevel;

        private DungeonTeammate(@Nullable final EntityPlayer player, @Nullable final DungeonListener.DungeonClass dungeonClass, final int classLevel) {
            super();

            this.player = player;
            this.dungeonClass = dungeonClass;
            this.classLevel = classLevel;
        }

        @Nullable
        final EntityPlayer getPlayer() {
            return this.player;
        }

        @Nullable
        final DungeonListener.DungeonClass getDungeonClass() {
            return this.dungeonClass;
        }

        final int getClassLevel() {
            return this.classLevel;
        }

        @Override
        public final int hashCode() {
            var result = Objects.hashCode(this.player);

            result = 31 * result + Objects.hashCode(this.dungeonClass);
            result = 31 * result + this.classLevel;

            return result;
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            return this == obj || obj instanceof final DungeonTeammate other && Objects.equals(this.player, other.player) && this.dungeonClass == other.dungeonClass && this.classLevel == other.classLevel;
        }

        @Override
        public final String toString() {
            return "DungeonTeammate{" +
                "player=" + this.player +
                ", dungeonClass=" + this.dungeonClass +
                ", classLevel=" + this.classLevel +
                '}';
        }
    }
}
