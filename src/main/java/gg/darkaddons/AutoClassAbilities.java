package gg.darkaddons;

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
                return -1L != DungeonTimer.getTerraClearTime();
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
                final var dungeonFloor = DungeonFeatures.getDungeonFloor();
                return ("M6".equals(dungeonFloor) || "F6".equals(dungeonFloor) ||
                    "M7".equals(dungeonFloor)) && -1L != DungeonTimer.getBossEntryTime();
            }
        },
        THUNDERSTORM(TimeUnit.SECONDS, 500L),
        RAGNAROK(TimeUnit.SECONDS, 60L) {
            @Override
            public final boolean forceCooldown() {
                final var dungeonFloor = DungeonFeatures.getDungeonFloor();
                return ("M6".equals(dungeonFloor) || "M5".equals(dungeonFloor) ||
                    "F6".equals(dungeonFloor)) && -1L != DungeonTimer.getBossEntryTime();
            }
        },
        RAPID_FIRE(TimeUnit.SECONDS, 100L) {
            @Override
            public final boolean forceCooldown() {
                final var dungeonFloor = DungeonFeatures.getDungeonFloor();
                return "M7".equals(dungeonFloor) && -1L != DungeonTimer.getBossEntryTime() || ("M6".equals(dungeonFloor) || "F6".equals(dungeonFloor)) && !AdditionalM7Features.isGiantsFalling() && -1L != DungeonTimer.getBossEntryTime();
            }
        },
        CASTLE_OF_STONE(TimeUnit.SECONDS, 150L) {
            @Override
            public final boolean forceCooldown() {
                final var dungeonFloor = DungeonFeatures.getDungeonFloorNumber();
                if (null != dungeonFloor && -1L != DungeonTimer.getBossEntryTime()) {
                    switch (dungeonFloor) {
                        case 7 -> {
                            return -1L == DungeonTimer.getPhase3ClearTime();
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

        private long cooldownInMs;
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

    static final void onWorldUnload() {
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
        return DarkAddons.isInDungeons() && -1L == DungeonTimer.getBossClearTime() && -1L != DungeonTimer.getDungeonStartTime();
    }

    private static final boolean checkPreconditions() {
        final var mc = Minecraft.getMinecraft();
        final var rc = mc.gameSettings.keyBindUseItem;

        return AutoClassAbilities.checkPrePreconditions() && (rc.isKeyDown() && AutoClicker.isHoldingTermOrRCM(mc) || mc.gameSettings.keyBindAttack.isKeyDown() && (AutoClicker.isHoldingClaymoreMidasOrGS(mc) || AutoClicker.isHoldingHype(mc)));
    }

    private static final void findClassAndAssignAbilities() {
        final var self = Minecraft.getMinecraft().thePlayer;
        for (final var teammate : DungeonListener.getTeam()) {
            //noinspection ObjectEquality
            if (self == teammate.getPlayer()) {
                final var dungeonClass = teammate.getDungeonClass();
                if (null == dungeonClass) {
                    // The dungeon probably did not start yet
                    break;
                }
                switch (dungeonClass) {
                    case ARCHER -> {
                        AutoClassAbilities.RegularClassAbility.EXPLOSIVE_SHOT.cooldownInMs = TimeUnit.SECONDS.toMillis(40L - (Math.min(50, teammate.getClassLevel()) / 5L << 1L));
                        AutoClassAbilities.regularClassAbility = AutoClassAbilities.RegularClassAbility.EXPLOSIVE_SHOT;

                        AutoClassAbilities.ultimateClassAbility = AutoClassAbilities.UltimateClassAbility.RAPID_FIRE;
                    }
                    case BERSERK -> {
                        AutoClassAbilities.regularClassAbility = AutoClassAbilities.RegularClassAbility.THROWING_AXE;
                        AutoClassAbilities.ultimateClassAbility = AutoClassAbilities.UltimateClassAbility.RAGNAROK;
                    }
                    case MAGE -> {
                        final var isDupeMage = 1 < DungeonListener.getTeam().stream().filter(t -> DungeonListener.DungeonClass.MAGE == t.getDungeonClass()).count();
                        var lvlExtraCdReduc = Math.min(50, teammate.getClassLevel()) >> 1;
                        if (!isDupeMage) {
                            lvlExtraCdReduc <<= 1;
                        }
                        final var baseCdReduc = 25;
                        final var totalCdReducMultiplier = 1.0D - (baseCdReduc + lvlExtraCdReduc) / 100.0D;
                        AutoClassAbilities.RegularClassAbility.GUIDED_SHEEP.cooldownInMs = TimeUnit.SECONDS.toMillis((long) (30.0D * totalCdReducMultiplier));
                        AutoClassAbilities.UltimateClassAbility.THUNDERSTORM.cooldownInMs = TimeUnit.SECONDS.toMillis((long) (500.0D * totalCdReducMultiplier));
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

            var shouldUse = AutoClassAbilities.UltimateClassAbility.CASTLE_OF_STONE == AutoClassAbilities.ultimateClassAbility;

            if (AutoClassAbilities.UltimateClassAbility.WISH == AutoClassAbilities.ultimateClassAbility) {
                shouldUse = 7 == DungeonFeatures.getDungeonFloorNumber();
            }

            if (shouldUse && AutoClassAbilities.ultimateClassAbility.isCooldownReallyGone()) {
                AutoClassAbilities.ultimateClassAbility.markUsed();
                AutoClassAbilities.emulateDropKeyPress(false);
            }
        }
    }
}
