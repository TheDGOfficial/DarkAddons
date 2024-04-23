package gg.darkaddons;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures;
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
import gg.skytils.skytilsmod.listeners.DungeonListener;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

final class AutoClassAbilities {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private AutoClassAbilities() {
        super();

        throw Utils.staticClassException();
    }

    private static int minimumTicksInBetween;

    @Nullable
    private static AutoClassAbilities.RegularClassAbility regularClassAbility;
    @Nullable
    private static AutoClassAbilities.UltimateClassAbility ultimateClassAbility;

    private interface ClassAbility {
        long getCooldownInMs();

        long getNextUse();

        void setNextUse(final long newNextUse);

        boolean forceCooldown();

        default void markUsed() {
            this.setNextUse(System.currentTimeMillis() + this.getCooldownInMs());
        }

        default boolean isCooldownReallyGone() {
            return System.currentTimeMillis() >= this.getNextUse();
        }

        default boolean isCooldownGone() {
            return this.isCooldownReallyGone() && !this.forceCooldown();
        }
    }

    private enum RegularClassAbility implements AutoClassAbilities.ClassAbility {
        HEALING_CIRCLE(TimeUnit.SECONDS, 2L),
        GUIDED_SHEEP(TimeUnit.SECONDS, 30L),
        THROWING_AXE(TimeUnit.SECONDS, 10L),
        EXPLOSIVE_SHOT(TimeUnit.SECONDS, 40L) {
            @Override
            public final boolean forceCooldown() {
                return -1L != DungeonTimer.INSTANCE.getTerraClearTime();
            }
        },
        SEISMIC_WAVE(TimeUnit.SECONDS, 15L);

        private long cooldownInMs;
        private long nextUse;

        private RegularClassAbility(@NotNull final TimeUnit unit, final long cooldown) {
            this.cooldownInMs = unit.toMillis(cooldown);
        }

        @Override
        public final long getCooldownInMs() {
            return this.cooldownInMs;
        }

        @Override
        public final long getNextUse() {
            return this.nextUse;
        }

        @Override
        public final void setNextUse(final long newNextUse) {
            this.nextUse = newNextUse;
        }

        @Override
        public boolean forceCooldown() {
            return false;
        }

        @Override
        public final String toString() {
            return "RegularClassAbility{" +
                "cooldownInMs=" + this.cooldownInMs +
                ", nextUse=" + this.nextUse +
                '}';
        }
    }

    private enum UltimateClassAbility implements AutoClassAbilities.ClassAbility {
        WISH(TimeUnit.SECONDS, 120L) {
            @Override
            public final boolean forceCooldown() {
                final var dungeonFloor = DungeonFeatures.INSTANCE.getDungeonFloor();
                return ("M6".equals(dungeonFloor) || "F6".equals(dungeonFloor) ||
                    "M7".equals(dungeonFloor)) && -1L != DungeonTimer.INSTANCE.getBossEntryTime();
            }
        },
        THUNDERSTORM(TimeUnit.SECONDS, 500L),
        RAGNAROK(TimeUnit.SECONDS, 60L) {
            @Override
            public final boolean forceCooldown() {
                final var dungeonFloor = DungeonFeatures.INSTANCE.getDungeonFloor();
                return ("M6".equals(dungeonFloor) || "M5".equals(dungeonFloor) ||
                    "F6".equals(dungeonFloor)) && -1L != DungeonTimer.INSTANCE.getBossEntryTime();
            }
        },
        RAPID_FIRE(TimeUnit.SECONDS, 100L) {
            @Override
            public final boolean forceCooldown() {
                final var dungeonFloor = DungeonFeatures.INSTANCE.getDungeonFloor();
                return "M7".equals(dungeonFloor) && -1L != DungeonTimer.INSTANCE.getBossEntryTime() || ("M6".equals(dungeonFloor) || "F6".equals(dungeonFloor)) && !AdditionalM7Features.isGiantsFalling() && -1L != DungeonTimer.INSTANCE.getBossEntryTime();
            }
        },
        CASTLE_OF_STONE(TimeUnit.SECONDS, 150L) {
            @Override
            public final boolean forceCooldown() {
                final var dungeonFloor = DungeonFeatures.INSTANCE.getDungeonFloorNumber();
                if (null != dungeonFloor && -1L != DungeonTimer.INSTANCE.getBossEntryTime()) {
                    switch (dungeonFloor) {
                        case 7 -> {
                            return -1L == DungeonTimer.INSTANCE.getPhase3ClearTime();
                        }
                        case 6 -> {
                            return !AdditionalM7Features.isGiantsFalling();
                        }
                        case 5 -> {
                            return !AdditionalM7Features.didLividsSpawn();
                        }
                    }
                }
                return true;
            }
        };

        private final long cooldownInMs;
        private long nextUse;

        private UltimateClassAbility(@NotNull final TimeUnit unit, final long cooldown) {
            this.cooldownInMs = unit.toMillis(cooldown);
        }

        @Override
        public final long getCooldownInMs() {
            return this.cooldownInMs;
        }

        @Override
        public final long getNextUse() {
            return this.nextUse;
        }

        @Override
        public final void setNextUse(final long newNextUse) {
            this.nextUse = newNextUse;
        }

        @Override
        public boolean forceCooldown() {
            return false;
        }

        @Override
        public final String toString() {
            return "UltimateClassAbility{" +
                "cooldownInMs=" + this.cooldownInMs +
                ", nextUse=" + this.nextUse +
                '}';
        }
    }

    static final void tick() {
        AutoClassAbilities.tick0();
    }

    private static final void tick0() {
        ++AutoClassAbilities.minimumTicksInBetween;

        if (40 <= AutoClassAbilities.minimumTicksInBetween) {
            AutoClassAbilities.minimumTicksInBetween = 0;
            AutoClassAbilities.useClassAbilitiesIfReadyOrUnknown();
        }
    }

    static final void worldLoad() {
        AutoClassAbilities.regularClassAbility = null;
        AutoClassAbilities.ultimateClassAbility = null;

        for (final var reg : AutoClassAbilities.RegularClassAbility.values()) {
            reg.setNextUse(0L);
        }

        for (final var ult : AutoClassAbilities.UltimateClassAbility.values()) {
            ult.setNextUse(0L);
        }
    }

    private static final boolean checkPrePreconditions() {
        return DarkAddons.isInDungeons() && -1L == DungeonTimer.INSTANCE.getBossClearTime() && -1L != DungeonTimer.INSTANCE.getDungeonStartTime();
    }

    private static final boolean checkPreconditions() {
        final var mc = Minecraft.getMinecraft();

        return AutoClassAbilities.checkPrePreconditions() && (mc.gameSettings.keyBindUseItem.isKeyDown() && AutoClicker.isHoldingTerm(mc) || mc.gameSettings.keyBindAttack.isKeyDown() && AutoClicker.isHoldingClaymoreOrGS(mc));
    }

    private static final void findClassAndAssignAbilities() {
        final var self = Minecraft.getMinecraft().thePlayer;
        for (final var teammate : DungeonListener.INSTANCE.getTeam().values()) {
            //noinspection ObjectEquality
            if (self == teammate.getPlayer()) {
                final var dungeonClass = teammate.getDungeonClass();
                switch (dungeonClass) {
                    case ARCHER -> {
                        AutoClassAbilities.RegularClassAbility.EXPLOSIVE_SHOT.cooldownInMs = TimeUnit.SECONDS.toMillis(40L - (teammate.getClassLevel() / 5L << 1L));
                        AutoClassAbilities.regularClassAbility = AutoClassAbilities.RegularClassAbility.EXPLOSIVE_SHOT;

                        AutoClassAbilities.ultimateClassAbility = AutoClassAbilities.UltimateClassAbility.RAPID_FIRE;
                    }
                    case BERSERK -> {
                        AutoClassAbilities.regularClassAbility = AutoClassAbilities.RegularClassAbility.THROWING_AXE;
                        AutoClassAbilities.ultimateClassAbility = AutoClassAbilities.UltimateClassAbility.RAGNAROK;
                    }
                    case MAGE -> {
                        AutoClassAbilities.regularClassAbility = AutoClassAbilities.RegularClassAbility.GUIDED_SHEEP;
                        AutoClassAbilities.ultimateClassAbility = AutoClassAbilities.UltimateClassAbility.THUNDERSTORM;
                    }
                    case HEALER -> {
                        AutoClassAbilities.regularClassAbility = AutoClassAbilities.RegularClassAbility.HEALING_CIRCLE;
                        AutoClassAbilities.ultimateClassAbility = AutoClassAbilities.UltimateClassAbility.WISH;
                    }
                    case TANK -> {
                        AutoClassAbilities.regularClassAbility = AutoClassAbilities.RegularClassAbility.SEISMIC_WAVE;
                        AutoClassAbilities.ultimateClassAbility = AutoClassAbilities.UltimateClassAbility.CASTLE_OF_STONE;
                    }
                    case EMPTY -> {
                        // the dungeon probably did not start yet
                    }
                }
                break;
            }
        }
    }

    private static final void emulateDropKeyPress(final boolean bulkDropWithCtrl) {
        final var self = Minecraft.getMinecraft().thePlayer;
        if (self.isSpectator()) {
            return;
        }
        self.dropOneItem(bulkDropWithCtrl);
    }

    private static final void useClassAbilitiesIfReadyOrUnknown() {
        if (AutoClassAbilities.checkPreconditions()) {
            if (Config.isAutoUltimateAbility()) {
                if (null == AutoClassAbilities.ultimateClassAbility) {
                    AutoClassAbilities.findClassAndAssignAbilities();
                }

                if (null != AutoClassAbilities.ultimateClassAbility && AutoClassAbilities.ultimateClassAbility.isCooldownGone()) {
                    AutoClassAbilities.ultimateClassAbility.markUsed();
                    AutoClassAbilities.emulateDropKeyPress(false);

                    return; // We can't use both ultimate and regular on the same tick.
                }
            }

            if (Config.isAutoRegularAbility()) {
                if (null == AutoClassAbilities.regularClassAbility) {
                    AutoClassAbilities.findClassAndAssignAbilities();
                }

                if (null != AutoClassAbilities.regularClassAbility && AutoClassAbilities.regularClassAbility.isCooldownGone()) {
                    AutoClassAbilities.regularClassAbility.markUsed();
                    AutoClassAbilities.emulateDropKeyPress(true);
                }
            }
        }
    }

    static final void ultReminderToAutoClassAbilitiesHook() {
        if (Config.isAutoUltimateAbility() && AutoClassAbilities.checkPrePreconditions()) {
            if (null == AutoClassAbilities.ultimateClassAbility) {
                AutoClassAbilities.findClassAndAssignAbilities();
            }

            boolean shouldUse = false;

            if (AutoClassAbilities.UltimateClassAbility.CASTLE_OF_STONE == AutoClassAbilities.ultimateClassAbility) {
                shouldUse = true;
            }

            if (AutoClassAbilities.UltimateClassAbility.WISH == AutoClassAbilities.ultimateClassAbility) {
                shouldUse = 7 == DungeonFeatures.INSTANCE.getDungeonFloorNumber();
            }

            if (shouldUse && AutoClassAbilities.ultimateClassAbility.isCooldownReallyGone()) {
                AutoClassAbilities.ultimateClassAbility.markUsed();
                AutoClassAbilities.emulateDropKeyPress(false);
            }
        }
    }
}
