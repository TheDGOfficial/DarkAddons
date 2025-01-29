package gg.darkaddons;

import com.google.common.collect.EvictingQueue;
import gg.skytils.skytilsmod.utils.SBInfo;
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo;
import gg.skytils.skytilsmod.utils.SkyblockIsland;
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import java.util.function.Supplier;
import java.util.function.IntSupplier;

import java.util.concurrent.Executors;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

final class SlayerRNGDisplay extends GuiElement {
    @NotNull
    private static final ArrayList<String> linesToRender = new ArrayList<>(11);
    private static int linesToRenderSize;

    private static final double getMeterProgress(@NotNull final SlayerRNGDisplay.SlayerDrop drop, final int meterXp) {
        return (double) meterXp / drop.requiredMeterXP * 100.0D;
    }

    private static final double getDropChance(@NotNull final SlayerRNGDisplay.SlayerDrop drop, final double meterProgressPercent) {
        if (100.0D <= meterProgressPercent) {
            return 100.0D;
        }

        final var meterMultiplier = 1.0D + 2.0D * meterProgressPercent / 100.0D;
        final var dropChanceWithMeter = drop.baseDropChance * meterMultiplier;

        return dropChanceWithMeter + dropChanceWithMeter / 100.0D * (5.0D <= drop.baseDropChance ? 1.0D : SlayerRNGDisplay.lastMagicFind);
    }

    private static final int getOdds(final double dropChance) {
        return (int) Math.round(100.0D / dropChance);
    }

    private static final long getAVGBossKillTime() {
        // Now, finally calculate the average.
        var sum = 0L;

        for (final long killTime : SlayerRNGDisplay.bossTimes) {
            sum += killTime;
        }

        return sum / Math.max(1, SlayerRNGDisplay.bossTimes.size());
    }

    @NotNull
    private static final String getAVGBossKillTimeFormatted(final long avgBossKillTime) {
        return Utils.formatTime(avgBossKillTime, true);
    }

    private static final long getETATime(@NotNull final SlayerRNGDisplay.SlayerDrop drop, final long avgBossKillTime, final int highestOdds, final int startingMeterXp, final int xpPerBoss) {
        // Simulates the increasing drop chances with each boss killed.

        var time = 0L;
        var xp = startingMeterXp;

        var odds = highestOdds;

        for (var i = 0; i < odds; ++i) {
            odds = SlayerRNGDisplay.getOdds(SlayerRNGDisplay.getDropChance(drop, SlayerRNGDisplay.getMeterProgress(drop, xp)));

            time += avgBossKillTime;
            xp += xpPerBoss;
        }

        return time;
    }

    private static final String formatETA(final long etaTime) {
        return Utils.formatTime(etaTime, false);
    }

    @NotNull
    private static final String enumName(@NotNull final String text) {
        final var enumName = Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase(Locale.ROOT);

        if (enumName.contains("_")) {
            var mutableName = enumName;

            while (mutableName.contains("_")) {
                mutableName = StringUtils.substringBefore(mutableName, "_") + ' ' + Character.toUpperCase(StringUtils.substringAfter(mutableName, "_").charAt(0)) + StringUtils.substringAfter(mutableName, "_").substring(1);
            }

            return mutableName;
        }

        return enumName;
    }

    private static final void header(@NotNull final SlayerRNGDisplay.Slayer slayer, @NotNull final SlayerRNGDisplay.SlayerDrop drop, final int selectedDropPrice) {
        SlayerRNGDisplay.linesToRender.add("§a⚔ Slayer: §5" + slayer.enumName + " §7- §d⚂ RNG: §6" + drop.getDisplayName() + (SlayerRNGDisplay.SellingMethod.NONE == drop.sellingMethod ? "" : " (" + Utils.formatNumber(selectedDropPrice) + " coins)"));
        SlayerRNGDisplay.linesToRender.add("");
    }

    private static final void dataNotAvailable() {
        SlayerRNGDisplay.linesToRender.add("§cPlease do a few bosses till the RNG Meter &");
        SlayerRNGDisplay.linesToRender.add("§cAVG Boss Kill Time data is available, and");
        SlayerRNGDisplay.linesToRender.add("§cdrop at least 1 rare drop to let the mod");
        SlayerRNGDisplay.linesToRender.add("§cknow your Magic Find!");
    }

    private static final void missingSlayerQuest() {
        SlayerRNGDisplay.linesToRender.add("§cPlease start a Slayer Quest to let the mod");
        SlayerRNGDisplay.linesToRender.add("§cknow what slayer you are doing!");
    }

    private static final void missingSlayerDrop() {
        SlayerRNGDisplay.linesToRender.add("§cThe mod couldn't determine the optimal drop");
        SlayerRNGDisplay.linesToRender.add("§cfor this slayer. This should not happen.");
        SlayerRNGDisplay.linesToRender.add("§cPlease report this as a bug!");
    }

    private static final void stats(final double meterProgressPercent, final double dropChance, final int odds, final long etaTime, final long avgBossKillTime, final int moneyPerHour, @NotNull final SlayerRNGDisplay.SlayerDrop drop) {
        SlayerRNGDisplay.linesToRender.add("§b✯ Magic Find: " + SlayerRNGDisplay.lastMagicFind);
        SlayerRNGDisplay.linesToRender.add("");
        SlayerRNGDisplay.linesToRender.add("§d⌛ RNG Meter: " + String.format(Locale.ROOT, "%.2f", Math.min(100.0D, meterProgressPercent)) + "% (" + Utils.formatNumber(SlayerRNGDisplay.lastMeterXP) + '/' + Utils.formatNumber(drop.requiredMeterXP) + ')');
        SlayerRNGDisplay.linesToRender.add("");
        SlayerRNGDisplay.linesToRender.add("§6♠ Odds: 1/" + odds + " (" + String.format(Locale.ROOT, "%.2f", dropChance) + "%)");
        SlayerRNGDisplay.linesToRender.add("");
        SlayerRNGDisplay.linesToRender.add("§e⌚ ETA: " + SlayerRNGDisplay.formatETA(etaTime) + " (" + SlayerRNGDisplay.getAVGBossKillTimeFormatted(avgBossKillTime) + " per boss on avg)");
        if (SlayerRNGDisplay.SellingMethod.NONE != drop.sellingMethod) {
            SlayerRNGDisplay.linesToRender.add("");
            SlayerRNGDisplay.linesToRender.add("§6$ Money/Hour: " + Utils.formatNumber(moneyPerHour) + " coins");
        }
    }

    private static final void update() {
        final var slayer = SlayerRNGDisplay.currentSlayer;

        if (null == slayer || !slayer.hasActiveQuest) {
            SlayerRNGDisplay.linesToRender.clear();
            SlayerRNGDisplay.missingSlayerQuest();
            SlayerRNGDisplay.updateWidthHeightSize();
            return;
        }

        final var drop = SlayerRNGDisplay.selectedDrop.get();

        if (null == drop) {
            SlayerRNGDisplay.linesToRender.clear();
            SlayerRNGDisplay.missingSlayerDrop();
            SlayerRNGDisplay.updateWidthHeightSize();
            return;
        }

        final var meterXP = SlayerRNGDisplay.lastMeterXP;
        final var meterProgressPercent = SlayerRNGDisplay.getMeterProgress(drop, meterXP);

        final var dropChance = SlayerRNGDisplay.getDropChance(drop, meterProgressPercent);
        final var odds = SlayerRNGDisplay.getOdds(dropChance);

        final var avgBossKillTime = SlayerRNGDisplay.getAVGBossKillTime();
        final var etaTime = SlayerRNGDisplay.getETATime(drop, avgBossKillTime, odds, meterXP, SlayerRNGDisplay.lastMeterXPGain);

        final var selectedDropPrice = drop.price;

        // Calculates money per hour with RNG meter after reset to be more realistic. If we calculate with current meter, it will be higher than actual.
        final var moneyPerHour = SlayerRNGDisplay.getMoneyPerHour(SlayerRNGDisplay.getETATime(drop, avgBossKillTime, SlayerRNGDisplay.getOdds(SlayerRNGDisplay.getDropChance(drop, SlayerRNGDisplay.getMeterProgress(drop, SlayerRNGDisplay.lastMeterXPGain))), SlayerRNGDisplay.lastMeterXPGain, SlayerRNGDisplay.lastMeterXPGain), selectedDropPrice);

        SlayerRNGDisplay.linesToRender.clear();

        SlayerRNGDisplay.header(slayer, drop, selectedDropPrice);

        if (0 == SlayerRNGDisplay.lastMagicFind || 0 == SlayerRNGDisplay.lastMeterXP || 0 == SlayerRNGDisplay.lastMeterXPGain || 0L == avgBossKillTime) {
            SlayerRNGDisplay.dataNotAvailable();
        } else {
            if (!drop.isEligibleToDrop()) {
                if (drop.isEligibleTier()) {
                    SlayerRNGDisplay.linesToRender.add("§cYou are not eligible to drop this RNG,");
                    SlayerRNGDisplay.linesToRender.add("§cbecause you are not at the required");
                    SlayerRNGDisplay.linesToRender.add("§clevel for this drop from this slayer yet.");
                    SlayerRNGDisplay.linesToRender.add("");
                } else {
                    SlayerRNGDisplay.linesToRender.add("§cYou are not eligible to drop this RNG,");
                    SlayerRNGDisplay.linesToRender.add("§cbecause you are not doing the required");
                    SlayerRNGDisplay.linesToRender.add("§ctier of the boss for this drop at the moment.");
                    SlayerRNGDisplay.linesToRender.add("");
                }
            }
            SlayerRNGDisplay.stats(meterProgressPercent, dropChance, odds, etaTime, avgBossKillTime, moneyPerHour, drop);
        }

        SlayerRNGDisplay.updateWidthHeightSize();
    }

    private static int width;
    private static int height;

    private static final void updateWidthHeightSize() {
        SlayerRNGDisplay.linesToRenderSize = SlayerRNGDisplay.linesToRender.size();

        SlayerRNGDisplay.width = GuiElement.getTextWidth(SlayerRNGDisplay.findLongestLine());
        SlayerRNGDisplay.height = GuiElement.getFontHeight() * SlayerRNGDisplay.linesToRenderSize;
    }

    @NotNull
    private static final String findLongestLine() {
        var longestLine = "";
        var longestLength = 0;

        for (final var line : SlayerRNGDisplay.linesToRender) {
            final var len = line.length();

            if (len > longestLength) {
                longestLine = line;
                longestLength = len;
            }
        }

        return longestLine;
    }

    @NotNull
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final int getMoneyPerHour(final long etaTime, final int coins) {
        // Calculates decimal hours, this allows for better accuracy, even if the result is rounded anyway.

        // If using integer hour, for example, when the eta is 7 hours and 59 minutes, for example, if we don't use decimal hour, the money per hour will be calculated with the 7 hours, thus ending up being more than the actual money per hour. If we do decimal hours, however, the result will be accurate and then rounded; the rounding offset is much smaller than a 59-minute offset.
        final var hours = etaTime / 1_000.0D / 60.0D / 60.0D;

        return 0.0D == hours ? 0 : (int) Math.round(coins / hours);
    }

    static final void forcePriceUpdateIfNecessary() {
        for (final var slayerDrop : SlayerRNGDisplay.SlayerDrop.values) {
            if (/*SlayerRNGDisplay.SellingMethod.AUCTION_HOUSE == slayerDrop.sellingMethod*/SlayerRNGDisplay.SellingMethod.NONE != slayerDrop.sellingMethod) {
                if (0 == slayerDrop.price) {
                    // This will cancel the previous registered task and register a new one, set to run with 0 delay for the first time and 1 minute intervals on subsequent ones.
                    SlayerRNGDisplay.registerPriceUpdateTask();
                    break;
                }
            }
        }
    }

    private static final Runnable priceUpdateTask = () -> {
            if (!Config.isSlayerRngDisplay() || DarkAddons.isInDungeons()) {
                return;
            }

            try {
                final var lbResp = Utils.sendWebRequest("https://lb.tricked.pro/lowestbins", "application/json", false);

                if (null == lbResp) {
                    // Likely no internet connection, bail out.
                    return;
                }

                final var lowestBINPrices = Utils.parseJsonObjectFromString(lbResp);
                var updateNeeded = false;

                for (final var slayerDrop : SlayerRNGDisplay.SlayerDrop.values) {
                    if (/*SlayerRNGDisplay.SellingMethod.AUCTION_HOUSE == slayerDrop.sellingMethod*/SlayerRNGDisplay.SellingMethod.NONE != slayerDrop.sellingMethod) {
                        if (0 == slayerDrop.price) {
                            // If any of the slayer drops that supposed to have a AH/BZ price has none, we need to update the display so that it does not keep showing 0 coins till the next time you kill a boss.
                            // Otherwise, no need to unnecessarily update the display - it will be updated the next time a slayer boss is done.
                            updateNeeded = true;
                        }
                        slayerDrop.price = lowestBINPrices.get(slayerDrop.getItemId()).getAsInt();
                    }
                }

                if (updateNeeded) {
                    SlayerRNGDisplay.updateNeeded = true;
                }

                /*final String bzResp = Utils.sendWebRequest("https://api.hypixel.net/skyblock/bazaar");

                if (null == bzResp) {
                    // Likely no internet connection, bail out.
                    return;
                }

                final JsonObject bazaarProducts = Utils.parseJsonObjectFromString(bzResp);

                for (final SlayerRNGDisplay.SlayerDrop slayerDrop : SlayerRNGDisplay.SlayerDrop.values) {
                    if (SlayerRNGDisplay.SellingMethod.BAZAAR == slayerDrop.sellingMethod) {
                        final JsonElement products = bazaarProducts.get("products");
                        if (null != products) {
                            final JsonElement product = products.getAsJsonObject().get(slayerDrop.getItemId());
                            if (null != product) {
                                final JsonElement sellSummary = product.getAsJsonObject().get("sell_summary");
                                if (null != sellSummary) {
                                    final JsonElement topOrder = sellSummary.getAsJsonArray().get(0);
                                    if (null != topOrder) {
                                        final JsonElement pricePerUnit = topOrder.getAsJsonObject().get("pricePerUnit");
                                        if (null != pricePerUnit) {
                                            slayerDrop.price = (int) Math.round(pricePerUnit.getAsDouble());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }*/
            } catch (final Throwable error) {
                DarkAddons.modError(error);
            }
    };

    @Nullable
    private static ScheduledFuture<?> scheduledPriceUpdateTask;

    private static final void registerPriceUpdateTask() {
        if (SlayerRNGDisplay.scheduledPriceUpdateTask != null) {
            SlayerRNGDisplay.scheduledPriceUpdateTask.cancel(false);
        }
        SlayerRNGDisplay.scheduledPriceUpdateTask = SlayerRNGDisplay.scheduler.scheduleWithFixedDelay(priceUpdateTask, 0L, 1L, TimeUnit.MINUTES);
    }

    private static final void registerQuestDetectionTask() {
        DarkAddons.registerTickTask("update_slayer_quest_detection", 4, true, () -> {
            if (!Config.isSlayerRngDisplay() || DarkAddons.isInDungeons()) {
                return;
            }

            var foundQuest = false;

            for (final var it = Arrays.asList(ScoreboardUtil.fetchScoreboardLines(12)).iterator(); it.hasNext();) {
                final var line = Utils.removeControlCodes(it.next()).trim();

                if ("Slayer Quest".equals(line)) {
                    if (it.hasNext()) {
                        final var boss = Utils.removeControlCodes(it.next()).trim();

                        for (final var slayer : SlayerRNGDisplay.Slayer.values) {
                            if (boss.startsWith(slayer.bossName)) {
                                if (!slayer.hasActiveQuest) {
                                    slayer.hasActiveQuest = true;

                                    SlayerRNGDisplay.updateNeeded = true;
                                }

                                final var tier = Utils.fastRomanToInt(StringUtils.remove(boss, slayer.bossName + ' '));

                                if (slayer.tier != tier) {
                                    slayer.tier = tier;

                                    SlayerRNGDisplay.updateNeeded = true;
                                }

                                if (SlayerRNGDisplay.currentSlayer != slayer) {
                                    if (null != SlayerRNGDisplay.currentSlayer) {
                                        // User changed the slayer they're doing, reset values.
                                        SlayerRNGDisplay.lastMagicFind = 0;
                                        SlayerRNGDisplay.lastMeterXP = 0;
                                        SlayerRNGDisplay.lastBossStartTime = 0L;
                                        SlayerRNGDisplay.lastMeterXPGain = 0;

                                        SlayerRNGDisplay.bossTimes.clear();
                                    }

                                    SlayerRNGDisplay.currentSlayer = slayer;

                                    SlayerRNGDisplay.updateNeeded = true;
                                }

                                foundQuest = true;
                            } else {
                                slayer.hasActiveQuest = false;
                                slayer.tier = 0;
                            }
                        }

                        break;
                    }

                    break;
                }
            }

            if (!foundQuest && null != SlayerRNGDisplay.currentSlayer) {
                SlayerRNGDisplay.currentSlayer.hasActiveQuest = false;
                SlayerRNGDisplay.currentSlayer.tier = 0;

                SlayerRNGDisplay.updateNeeded = true;
            }
        });
    }

    private final void registerHUDUpdateTask() {
        DarkAddons.registerTickTask("update_slayer_rng_display", 4, true, () -> {
            if (this.isEnabled() && SlayerRNGDisplay.updateNeeded) {
                SlayerRNGDisplay.updateNeeded = false;

                SlayerRNGDisplay.update();
            }
        });
    }

    SlayerRNGDisplay() {
        super("Slayer RNG Display");

        SlayerRNGDisplay.registerPriceUpdateTask();
        SlayerRNGDisplay.registerQuestDetectionTask();

        this.registerHUDUpdateTask();
    }

    @Override
    final void render(final boolean demo) {
        final var slayer = SlayerRNGDisplay.currentSlayer;

        if (demo || this.isEnabled() && !DarkAddons.isInLocationEditingGui() && null != slayer && slayer.hasActiveQuest && slayer.isInSlayerArea()) {
            final var leftAlign = this.shouldLeftAlign();
            final var alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
            final var xPos = leftAlign ? 0.0F : this.getWidth(demo);

            final var shadow = SmartFontRenderer.TextShadow.NONE;

            final var fontHeight = GuiElement.getFontHeight();
            final var color = CommonColors.Companion.getWHITE();

            final var length = SlayerRNGDisplay.linesToRenderSize;

            for (var i = 0; i < length; ++i) {
                GuiElement.drawString(
                    SlayerRNGDisplay.linesToRender.get(i),
                    xPos,
                    i * fontHeight,
                    color,
                    alignment,
                    shadow
                );
            }

        }
    }

    @Override
    final boolean isEnabled() {
        return Config.isSlayerRngDisplay();
    }

    @Override
    final int getHeight() {
        return SlayerRNGDisplay.height;
    }

    @Override
    final int getWidth(final boolean demo) {
        return SlayerRNGDisplay.width;
    }

    static final void onReceiveChatMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("slayer_rng_display_on_message");

        if (Config.isSlayerRngDisplay() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            SlayerRNGDisplay.parseMessage(event.message.getFormattedText(), Utils.removeControlCodes(event.message.getUnformattedText()).trim());
        }

        McProfilerHelper.endSection();
    }

    private enum Slayer {
        ZOMBIE(new SkyblockIsland[]{SkyblockIsland.Hub}, "Revenant Horror"),
        SPIDER(new SkyblockIsland[]{SkyblockIsland.SpiderDen, SkyblockIsland.CrimsonIsle}, "Tarantula Broodfather"),
        WOLF(new SkyblockIsland[]{SkyblockIsland.ThePark, SkyblockIsland.Hub}, "Sven Packmaster"),
        ENDERMAN(new SkyblockIsland[]{SkyblockIsland.TheEnd}, "Voidgloom Seraph"),
        BLAZE(new SkyblockIsland[]{SkyblockIsland.CrimsonIsle}, "Inferno Demonlord"),
        VAMPIRE(new SkyblockIsland[]{SkyblockIsland.TheRift}, "Riftstalker Bloodfiend");

        @NotNull
        private final SkyblockIsland[] slayerAreas;
        @NotNull
        private final String bossName;
        private boolean hasActiveQuest;
        private int tier;
        private int level;
        @NotNull
        private final String enumName = SlayerRNGDisplay.enumName(this.name());
        @NotNull
        private static final SlayerRNGDisplay.Slayer[] values = SlayerRNGDisplay.Slayer.values();

        private Slayer(@NotNull final SkyblockIsland[] slayerAreasIn, @NotNull final String bossNameIn) {
            this.slayerAreas = slayerAreasIn;
            this.bossName = bossNameIn;
        }

        private final boolean isInSlayerArea() {
            for (final var slayerArea : this.slayerAreas) {
                if (slayerArea.getMode().equals(SBInfo.INSTANCE.getMode())) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public final String toString() {
            return "Slayer{" +
                "slayerAreas=" + Arrays.toString(this.slayerAreas) +
                ", bossName='" + this.bossName + '\'' +
                ", hasActiveQuest=" + this.hasActiveQuest +
                ", tier=" + this.tier +
                ", level=" + this.level +
                ", enumName='" + this.enumName + '\'' +
                '}';
        }
    }

    @Nullable
    private static SlayerRNGDisplay.Slayer currentSlayer;

    private enum SellingMethod {
        AUCTION_HOUSE, BAZAAR, NONE;

        private SellingMethod() {
        }
    }

    private enum SlayerDrop {
        WARDEN_HEART(3_631_749, 0.013_8D, SlayerRNGDisplay.Slayer.ZOMBIE, 7, 5),
        TARANTULA_TALISMAN(300_550, 0.166_4D, SlayerRNGDisplay.Slayer.SPIDER, 6, 3),
        OVERFLUX_CAPACITOR(1_232_700, 0.040_6D, SlayerRNGDisplay.Slayer.WOLF, 7, 4),
        JUDGEMENT_CORE(885_562, 0.056_5D, SlayerRNGDisplay.Slayer.ENDERMAN, 7, 4),

        DUPLEX_I_BOOK(/*"ENCHANTMENT_ULTIMATE_REITERATE_1"*/"ENCHANTED_BOOK-ULTIMATE_REITERATE-1", 23_220, 2.153_3D, SlayerRNGDisplay.Slayer.BLAZE, 6, 4, 3, SlayerRNGDisplay.SellingMethod.BAZAAR),

        HIGH_CLASS_ARCHFIEND_DICE(194_939, 0.256_5D, SlayerRNGDisplay.Slayer.BLAZE, 7, 4, () -> Config.isPrioritizeDice() ? 5 : "Aatrox".equals(MayorInfo.INSTANCE.getCurrentMayor()) ? 4 : 2),

        GABAGOOL_DISTILLATE("CRUDE_GABAGOOL_DISTILLATE", 10_649, 4.695_2D, SlayerRNGDisplay.Slayer.BLAZE, 3, 2, () -> "Derpy".equals(MayorInfo.INSTANCE.getCurrentMayor()) ? 4 : 1, SlayerRNGDisplay.SellingMethod.BAZAAR),

        MC_GRUBBERS_BURGER(18_450, 1.219_5D, SlayerRNGDisplay.Slayer.VAMPIRE, 5, 4, () -> Config.isBurgersDone() ? 1 : 4, SlayerRNGDisplay.SellingMethod.NONE, "McGrubber's Burger"),

        UNFANGED_VAMPIRE_PART("VAMPIRE_DENTIST_RELIC", 18_450, 1.219_5D, SlayerRNGDisplay.Slayer.VAMPIRE, 5, 4, 2),
        THE_ONE_IV_BOOK(/*"ENCHANTMENT_ULTIMATE_THE_ONE_4"*/"ENCHANTED_BOOK-ULTIMATE_THE_ONE-4", 12_525, 1.796_4D, SlayerRNGDisplay.Slayer.VAMPIRE, 5, 5, 3, SlayerRNGDisplay.SellingMethod.BAZAAR);

        @Nullable
        private final String itemId;
        private final int requiredMeterXP;
        private final double baseDropChance;
        @NotNull
        private final SlayerRNGDisplay.Slayer slayer;
        private final int requiredLevel;
        private final int requiredTier;
        @NotNull
        private final IntSupplier profitWeight;
        @NotNull
        private final SlayerRNGDisplay.SellingMethod sellingMethod;
        @Nullable
        private final String displayName;
        private int price;
        @NotNull
        private final String enumName = SlayerRNGDisplay.enumName(this.name());
        @NotNull
        private static final SlayerRNGDisplay.SlayerDrop[] values = SlayerRNGDisplay.SlayerDrop.values();

        private SlayerDrop(final int requiredMeterXPIn, final double baseDropChanceIn, @NotNull final SlayerRNGDisplay.Slayer slayerIn, final int requiredLevelIn, final int requiredTierIn) {
            this(requiredMeterXPIn, baseDropChanceIn, slayerIn, requiredLevelIn, requiredTierIn, -1, SlayerRNGDisplay.SellingMethod.AUCTION_HOUSE);
        }

        private SlayerDrop(final int requiredMeterXPIn, final double baseDropChanceIn, @NotNull final SlayerRNGDisplay.Slayer slayerIn, final int requiredLevelIn, final int requiredTierIn, final int profitWeightIn, @NotNull final SlayerRNGDisplay.SellingMethod sellingMethodIn) {
            this(null, requiredMeterXPIn, baseDropChanceIn, slayerIn, requiredLevelIn, requiredTierIn, () -> profitWeightIn, sellingMethodIn, null);
        }

        private SlayerDrop(final int requiredMeterXPIn, final double baseDropChanceIn, @NotNull final SlayerRNGDisplay.Slayer slayerIn, final int requiredLevelIn, final int requiredTierIn, @NotNull final IntSupplier profitWeightIn) {
            this(null, requiredMeterXPIn, baseDropChanceIn, slayerIn, requiredLevelIn, requiredTierIn, profitWeightIn, SlayerRNGDisplay.SellingMethod.AUCTION_HOUSE, null);
        }

        private SlayerDrop(final int requiredMeterXPIn, final double baseDropChanceIn, @NotNull final SlayerRNGDisplay.Slayer slayerIn, final int requiredLevelIn, final int requiredTierIn, @NotNull final IntSupplier profitWeightIn, @NotNull final SlayerRNGDisplay.SellingMethod sellingMethodIn, @NotNull final String displayNameIn) {
            this(null, requiredMeterXPIn, baseDropChanceIn, slayerIn, requiredLevelIn, requiredTierIn, profitWeightIn, sellingMethodIn, displayNameIn);
        }

        private SlayerDrop(@Nullable final String itemIdIn, final int requiredMeterXPIn, final double baseDropChanceIn, @NotNull final SlayerRNGDisplay.Slayer slayerIn, final int requiredLevelIn, final int requiredTierIn, final int profitWeightIn) {
            this(itemIdIn, requiredMeterXPIn, baseDropChanceIn, slayerIn, requiredLevelIn, requiredTierIn, () -> profitWeightIn, SlayerRNGDisplay.SellingMethod.AUCTION_HOUSE, null);
        }

        private SlayerDrop(@Nullable final String itemIdIn, final int requiredMeterXPIn, final double baseDropChanceIn, @NotNull final SlayerRNGDisplay.Slayer slayerIn, final int requiredLevelIn, final int requiredTierIn, final int profitWeightIn, @NotNull final SlayerRNGDisplay.SellingMethod sellingMethodIn) {
            this(itemIdIn, requiredMeterXPIn, baseDropChanceIn, slayerIn, requiredLevelIn, requiredTierIn, () -> profitWeightIn, sellingMethodIn, null);
        }

        private SlayerDrop(@Nullable final String itemIdIn, final int requiredMeterXPIn, final double baseDropChanceIn, @NotNull final SlayerRNGDisplay.Slayer slayerIn, final int requiredLevelIn, final int requiredTierIn, @NotNull final IntSupplier profitWeightIn, @NotNull final SlayerRNGDisplay.SellingMethod sellingMethodIn) {
            this(itemIdIn, requiredMeterXPIn, baseDropChanceIn, slayerIn, requiredLevelIn, requiredTierIn, profitWeightIn, sellingMethodIn, null);
        }

        private SlayerDrop(@Nullable final String itemIdIn, final int requiredMeterXPIn, final double baseDropChanceIn, @NotNull final SlayerRNGDisplay.Slayer slayerIn, final int requiredLevelIn, final int requiredTierIn, @NotNull final IntSupplier profitWeightIn, @NotNull final SlayerRNGDisplay.SellingMethod sellingMethodIn, @Nullable final String displayNameIn) {
            this.itemId = itemIdIn;
            this.requiredMeterXP = requiredMeterXPIn;
            this.baseDropChance = baseDropChanceIn;
            this.slayer = slayerIn;
            this.requiredLevel = requiredLevelIn;
            this.requiredTier = requiredTierIn;
            this.profitWeight = profitWeightIn;
            this.sellingMethod = sellingMethodIn;
            this.displayName = displayNameIn;
        }

        @NotNull
        private final String getItemId() {
            final var nullableId = this.itemId;

            return null == nullableId ? this.name() : nullableId;
        }

        private final boolean isEligibleToDrop() {
            return this.isEligibleTier() && this.isEligibleLevel();
        }

        private final boolean isEligibleTier() {
            final var nullableSlayer = SlayerRNGDisplay.currentSlayer;

            return null != nullableSlayer && nullableSlayer.tier >= this.requiredTier;
        }

        private final boolean isEligibleLevel() {
            final var nullableSlayer = SlayerRNGDisplay.currentSlayer;

            return null != nullableSlayer && nullableSlayer.level >= this.requiredLevel;
        }

        @NotNull
        private final String getDisplayName() {
            final var nullableDisplayName = this.displayName;

            return null == nullableDisplayName ? this.enumName : nullableDisplayName;
        }

        @Override
        public final String toString() {
            return "SlayerDrop{" +
                "itemId='" + this.itemId + '\'' +
                ", requiredMeterXP=" + this.requiredMeterXP +
                ", baseDropChance=" + this.baseDropChance +
                ", slayer=" + this.slayer +
                ", requiredLevel=" + this.requiredLevel +
                ", requiredTier=" + this.requiredTier +
                ", profitWeight=" + this.profitWeight +
                ", sellingMethod=" + this.sellingMethod +
                ", displayName='" + this.displayName + '\'' +
                ", price=" + this.price +
                ", enumName='" + this.enumName + '\'' +
                '}';
        }
    }

    @NotNull
    private static final Supplier<SlayerRNGDisplay.SlayerDrop> selectedDrop = () -> {
        final var slayer = SlayerRNGDisplay.currentSlayer;

        if (null != slayer) {
            @SuppressWarnings("TypeMayBeWeakened") final var drops = new ArrayList<SlayerRNGDisplay.SlayerDrop>(3);
            @SuppressWarnings("TypeMayBeWeakened") final var eligibleDrops = new ArrayList<SlayerRNGDisplay.SlayerDrop>(3);

            for (final var drop : SlayerRNGDisplay.SlayerDrop.values) {
                if (drop.slayer == slayer) {
                    if (-1 == drop.profitWeight.getAsInt()) {
                        // Early exit; special weight for slayers that only have 1 good drop
                        return drop;
                    }

                    drops.add(drop);

                    if (drop.isEligibleToDrop()) {
                        eligibleDrops.add(drop);
                    }
                }
            }

            SlayerRNGDisplay.SlayerDrop highest = null;
            for (final var drop : eligibleDrops) {
                if (null == highest || highest.profitWeight.getAsInt() < drop.profitWeight.getAsInt()) {
                    highest = drop;
                }
            }

            if (null != highest) {
                return highest;
            }

            // If no drops are eligible, default to a non-eligible most-profit drop. Even if they can't drop it yet, they can see their RNG meter progress, better than showing nothing, I guess.
            for (final var drop : drops) {
                if (null == highest || highest.profitWeight.getAsInt() < drop.profitWeight.getAsInt()) {
                    highest = drop;
                }
            }

            return highest;
        }

        return null;
    };

    private static int lastMagicFind;
    private static int lastMeterXP;

    private static long lastBossStartTime;

    private static int lastMeterXPGain;

    @NotNull
    private static final EvictingQueue<Long> bossTimes = EvictingQueue.create(5);

    private static boolean updateNeeded = true;

    static final void markUpdateNeeded() {
        SlayerRNGDisplay.updateNeeded = true;
    }

    private static final void parseMessage(@NotNull final String originalMessage, @NotNull final String message) {
        // The originalMessage checks are necessary to make sure it's the colored message sent by Hypixel and not a message sent by players in all, party, guild chat etc., when pasting their RNG drops, for example.
        if (message.endsWith("% ✯ Magic Find)") && message.contains(") (+") && originalMessage.contains(" §r§b(+")) {
            SlayerRNGDisplay.lastMagicFind = Utils.safeParseIntFast(StringUtils.substringAfter(StringUtils.substringBefore(message, "% ✯ Magic Find)"), ") (+"));
            SlayerRNGDisplay.updateNeeded = true;
        } else if (message.startsWith("RNG Meter - ") && originalMessage.contains("§dRNG Meter")) {
            final var xpBefore = SlayerRNGDisplay.lastMeterXP;
            final var xpNow = Utils.safeParseIntFast(StringUtils.remove(StringUtils.remove(StringUtils.remove(message, "RNG Meter - "), " Stored XP"), ","));

            if (0 != xpBefore) {
                // If the meter got reset, the current XP will be lower than previous XP and the current XP will be our XP per boss. Instead of a hardcoded value like 625 / 500 and checking if the mayor is Aatrox, this is a simpler calculation that will work for all Slayer tiers since they all give different XP (and some slayers like Vampire give different XP than others in each tier).
                SlayerRNGDisplay.lastMeterXPGain = xpNow > xpBefore ? xpNow - xpBefore : xpNow;
            }

            SlayerRNGDisplay.lastMeterXP = xpNow;
            SlayerRNGDisplay.updateNeeded = true;
        } else if (message.startsWith("SLAYER QUEST STARTED!") && originalMessage.contains("§5§lSLAYER QUEST STARTED!")) {
            SlayerRNGDisplay.lastBossStartTime = System.currentTimeMillis();
        } else if (0L != SlayerRNGDisplay.lastBossStartTime && message.startsWith("SLAYER QUEST COMPLETE!") && originalMessage.contains("§a§lSLAYER QUEST COMPLETE!")) {
            SlayerRNGDisplay.bossTimes.add(System.currentTimeMillis() - SlayerRNGDisplay.lastBossStartTime);
            SlayerRNGDisplay.updateNeeded = true;
        } else {
            SlayerRNGDisplay.parseMessage0(originalMessage, message);
        }
    }

    private static final void parseMessage0(@NotNull final String originalMessage, @NotNull final String message) {
        final var slayerLVLText = " Slayer LVL ";

        for (final var slayer : SlayerRNGDisplay.Slayer.values) {
            if (message.startsWith(slayer.enumName + slayerLVLText) && originalMessage.contains("§r§e" + slayer.enumName + slayerLVLText)) {
                slayer.level = Utils.safeParseIntFast(Character.toString(StringUtils.substringAfter(message, slayer.enumName + slayerLVLText).charAt(0)));

                break;
            }
        }
    }
}
