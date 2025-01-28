package gg.darkaddons;

import java.util.UUID;
import java.util.ArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

import org.apache.commons.lang3.StringUtils;

final class UpgradeChecker {
    @SuppressWarnings("LambdaCanBeReplacedWithAnonymous")
    @NotNull
    private static final ExecutorService upgradeCheckerExecutor = Executors.newSingleThreadExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons Upgrade Checker Thread"));

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private UpgradeChecker() {
        super();

        throw Utils.staticClassException();
    }

    private enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY,
        MYTHIC,
        DIVINE,
        SPECIAL,
        VERY_SPECIAL,
        ULTIMATE
    }

    private enum UpgradeSource {
        ESSENCE_SHOP,
        ACCESSORY_BAG,
        PETS_MENU,
        ARMOR,
        EQUIPMENT,
        WEAPON,
        MISC
    }

    private enum UpgradeType {
        MORE_DPS,
        LESS_DMG,
        MORE_RNG,
        MORE_XP
    }

    private abstract static class Upgrade {
        private final UpgradeChecker.UpgradeSource source;
        private final UpgradeChecker.UpgradeType type;
        private final UpgradeChecker.Upgrade[] dependencies;
        boolean found;

        private Upgrade(@NotNull final UpgradeChecker.UpgradeSource source, @NotNull final UpgradeChecker.UpgradeType type, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            super();

            this.source = source;
            this.type = type;
            this.dependencies = dependencies;
        }
    }

    private static final class Accessory extends UpgradeChecker.Upgrade {
        private static final ArrayList<UpgradeChecker.Accessory> allAccessories = new ArrayList<>(16);

        static {
            registerAccessory(UpgradeChecker.UpgradeType.LESS_DMG, "WITHER_RELIC", "-25% Damage from Withers");
            registerAccessory(UpgradeChecker.UpgradeType.MORE_XP, "SCARF_GRIMOIRE", "+6% Class XP");
            registerAccessory(UpgradeChecker.UpgradeType.MORE_RNG, "TREASURE_ARTIFACT", "+3% RNG");
            registerAccessory(UpgradeChecker.UpgradeType.MORE_XP, "CATACOMBS_EXPERT_RING", "+10% Catacombs XP");
            registerAccessory(UpgradeChecker.UpgradeType.MORE_DPS, "DRACONIC_ARTIFACT", "+5% Damage against Dragons");
            registerAccessory(UpgradeChecker.UpgradeType.MORE_DPS, "MASTER_SKULL_TIER_7", "+10% Strength and Health in MM Catacombs");
            registerAccessory(UpgradeChecker.UpgradeType.MORE_DPS, "BOOK_OF_PROGRESSION", "Extra Infliction Passive", lore -> Utils.removeControlCodes(lore.stream().filter(line -> line.endsWith(" Passive")).findFirst().orElseThrow(() -> new IllegalStateException("Couldn't find Book of Progression bonus!"))), "Up to +5% Damage");
            registerAccessory(UpgradeChecker.UpgradeType.MORE_DPS, "HANDY_BLOOD_CHALICE", "On", lore -> Utils.removeControlCodes(StringUtils.removeStart(lore.stream().filter(line -> line.startsWith("§7Enabled: ")).findFirst().orElseThrow(() -> new IllegalStateException("Couldn't find Book of Progression bonus!")), "§7Enabled: ")), "+20 Ferocity");
            registerAccessory(UpgradeChecker.UpgradeType.MORE_DPS, "GENERAL_MEDALLION", "+6%", lore -> StringUtils.removeStart(lore.stream().filter(line -> !line.isEmpty() && line.charAt(line.length() - 1) == '%').findFirst().orElseThrow(() -> new IllegalStateException("Couldn't find General's Medallion bonus!")), "§7Bonus: §a"), "+6% Item Stats");
        }

        private static final void registerAccessory(@NotNull final UpgradeChecker.UpgradeType upgradeType, @NotNull final String internalName, @NotNull final String description, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            allAccessories.add(new UpgradeChecker.Accessory(upgradeType, internalName, null, null, description, dependencies));
        }

        private static final void registerAccessory(@NotNull final UpgradeChecker.UpgradeType upgradeType, @NotNull final String internalName, @Nullable final String loreCondition, @Nullable final Function<ArrayList<String>, String> loreExtractor, @NotNull final String description, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            allAccessories.add(new UpgradeChecker.Accessory(upgradeType, internalName, loreCondition, loreExtractor, description, dependencies));
        }

        private final String internalName;
        private final String description;

        private final String loreCondition;
        private final Function<ArrayList<String>, String> loreExtractor;

        private Accessory(@NotNull final UpgradeChecker.UpgradeType upgradeType, @NotNull final String internalName, @Nullable final String loreCondition, @Nullable final Function<ArrayList<String>, String> loreExtractor, @NotNull final String description, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            super(UpgradeChecker.UpgradeSource.ACCESSORY_BAG, upgradeType, dependencies);

            this.internalName = internalName;
            this.description = description;

            this.loreCondition = loreCondition;
            this.loreExtractor = loreExtractor;
        }
    }

    private static final class Pet extends UpgradeChecker.Upgrade {
        private static final ArrayList<UpgradeChecker.Pet> allPets = new ArrayList<>(16);

        static {
            registerPet(UpgradeChecker.UpgradeType.MORE_DPS, "GOLDEN_DRAGON", UpgradeChecker.Rarity.LEGENDARY, UpgradeChecker.Rarity.LEGENDARY, 200, 200);
            registerPet(UpgradeChecker.UpgradeType.MORE_DPS, "ENDER_DRAGON", UpgradeChecker.Rarity.LEGENDARY, UpgradeChecker.Rarity.LEGENDARY, 100, 100, "CROCHET_TIGER_PLUSHIE");
            registerPet(UpgradeChecker.UpgradeType.MORE_DPS, "JELLYFISH", UpgradeChecker.Rarity.LEGENDARY, UpgradeChecker.Rarity.LEGENDARY, 100, 100);
            registerPet(UpgradeChecker.UpgradeType.LESS_DMG, "PHOENIX", UpgradeChecker.Rarity.EPIC, UpgradeChecker.Rarity.LEGENDARY, 1, 100, "CROCHET_TIGER_PLUSHIE");
            registerPet(UpgradeChecker.UpgradeType.MORE_RNG, "SPIRIT", UpgradeChecker.Rarity.LEGENDARY, UpgradeChecker.Rarity.LEGENDARY, 1, 100);
        }

        private static final void registerPet(@NotNull final UpgradeChecker.UpgradeType upgradeType, @NotNull final String internalName, @NotNull final UpgradeChecker.Rarity minimumWantedRarity, @NotNull final UpgradeChecker.Rarity maxRarity, final int minimumWantedLevel, final int maxLevel, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            registerPet(upgradeType, internalName, minimumWantedRarity, maxRarity, minimumWantedLevel, maxLevel, null, dependencies);
        }

        private static final void registerPet(@NotNull final UpgradeChecker.UpgradeType upgradeType, @NotNull final String internalName, @NotNull final UpgradeChecker.Rarity minimumWantedRarity, @NotNull final UpgradeChecker.Rarity maxRarity, final int minimumWantedLevel, final int maxLevel, @Nullable final String optimalPetItem, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            allPets.add(new UpgradeChecker.Pet(upgradeType, internalName, minimumWantedRarity, maxRarity, minimumWantedLevel, maxLevel, optimalPetItem, dependencies));
        }

        private final String internalName;

        private UpgradeChecker.Rarity currentRarity;

        private final UpgradeChecker.Rarity minimumWantedRarity;
        private final UpgradeChecker.Rarity maxRarity;

        private int currentLevel;

        private final int minimumWantedLevel;
        private final int maxLevel;

        private final String optimalPetItem;

        private Pet(@NotNull final UpgradeChecker.UpgradeType upgradeType, @NotNull final String internalName, @NotNull final UpgradeChecker.Rarity minimumWantedRarity, @NotNull final UpgradeChecker.Rarity maxRarity, final int minimumWantedLevel, final int maxLevel, @Nullable final String optimalPetItem, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            super(UpgradeChecker.UpgradeSource.PETS_MENU, upgradeType, dependencies);

            this.internalName = internalName;
            this.minimumWantedRarity = minimumWantedRarity;
            this.maxRarity = maxRarity;
            this.minimumWantedLevel = minimumWantedLevel;
            this.maxLevel = maxLevel;
            this.optimalPetItem = optimalPetItem;
        }
    }

    private static final class EssenceShopPerk extends UpgradeChecker.Upgrade {
        private static final ArrayList<UpgradeChecker.EssenceShopPerk> allPerks = new ArrayList<>(16);

        static {
            registerPerk(UpgradeChecker.UpgradeType.MORE_DPS, "forbidden_blessing", 10, level -> "+" + level + "% stats from Blessings");
            registerPerk(UpgradeChecker.UpgradeType.MORE_DPS, "catacombs_crit_damage", 5, level -> "+" + level * 10 + " Crit Damage");
            registerPerk(UpgradeChecker.UpgradeType.MORE_DPS, "catacombs_strength", 5, level -> "+" + level * 10 + " Strength");
            registerPerk(UpgradeChecker.UpgradeType.MORE_DPS, "edrag_cd", 5, level -> "+" + level * 2 + " Crit Damage on Ender Dragon pet", UpgradeChecker.Pet.allPets.stream().filter(pet -> "ENDER_DRAGON".equals(pet.internalName)).findFirst().orElseThrow(() -> new IllegalStateException("Couldn't find Ender Dragon in Pets!")));
            registerPerk(UpgradeChecker.UpgradeType.MORE_DPS, "permanent_strength", 5, level -> "+" + level + " Strength");
            registerPerk(UpgradeChecker.UpgradeType.MORE_DPS, "fero_vs_dragons", 5, level -> "+" + level * 2 + " Ferocity against Dragons");
            registerPerk(UpgradeChecker.UpgradeType.MORE_DPS, "blessing_of_time", 3, level -> "+" + level * 2 + " Strength");

            registerPerk(UpgradeChecker.UpgradeType.MORE_RNG, "catacombs_boss_luck", 4, level -> "+" + (3 == level ? 5 : 2 == level ? 7 : 9) + "% RNG");
            registerPerk(UpgradeChecker.UpgradeType.MORE_RNG, "catacombs_looting", 5, level -> "+" + level * 2 + "% quality on Mob drops");

            registerPerk(UpgradeChecker.UpgradeType.MORE_XP, "unbridled_rage", 5, level -> "+" + level * 2 + "% Berserker Class XP");
            registerPerk(UpgradeChecker.UpgradeType.MORE_XP, "toxophilite", 5, level -> "+" + level * 2 + "% Archer Class XP");
            registerPerk(UpgradeChecker.UpgradeType.MORE_XP, "diamond_in_the_rough", 5, level -> "+" + level * 2 + "% Tank Class XP");
            registerPerk(UpgradeChecker.UpgradeType.MORE_XP, "heart_of_gold", 5, level -> "+" + level * 2 + "% Healer Class XP");
            registerPerk(UpgradeChecker.UpgradeType.MORE_XP, "cold_efficiency", 5, level -> "+" + level * 2 + "% Mage Class XP");
        }

        private static final void registerPerk(@NotNull final UpgradeChecker.UpgradeType upgradeType, @NotNull final String internalName, final int maxLevel, @NotNull final Function<Integer, String> descriptionAtLevel, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            allPerks.add(new UpgradeChecker.EssenceShopPerk(upgradeType, internalName, maxLevel, descriptionAtLevel, dependencies));
        }

        private final String internalName;

        private int currentLevel;
        private final int maxLevel;

        private final Function<Integer, String> descriptionAtLevel;

        private EssenceShopPerk(@NotNull final UpgradeChecker.UpgradeType upgradeType, @NotNull final String internalName, final int maxLevel, @NotNull final Function<Integer, String> descriptionAtLevel, @NotNull final UpgradeChecker.Upgrade... dependencies) {
            super(UpgradeChecker.UpgradeSource.ESSENCE_SHOP, upgradeType, dependencies);

            this.internalName = internalName;
            this.maxLevel = maxLevel;
            this.descriptionAtLevel = descriptionAtLevel;
        }
    }

    @NotNull
    private static final ArrayList<String> getLore(@NotNull final JsonObject tag) {
        final var loreList = new ArrayList<String>(16);
        for (final var lore : tag.get("display").getAsJsonObject().get("Lore").getAsJsonArray()) {
            loreList.add(lore.getAsString());
        }
        return loreList;
    }

    private static final boolean loreContains(@NotNull final ArrayList<String> loreLines, @NotNull final String search) {
        for (final var lore : loreLines) {
            if (lore.contains(search)) {
                return true;
            }
        }
        return false;
    }

    private static final boolean checkDependencies(@NotNull final UpgradeChecker.Upgrade upgrade) {
        for (final var dependency : upgrade.dependencies) {
            if (!dependency.found) {
                return false;
            }
        }
        return true;
    }

    private static final void processPet(@NotNull final Consumer<String> outputConsumer, @NotNull final JsonObject pet, @NotNull final UpgradeChecker.Pet registryPet, @NotNull final String internalName) {
        registryPet.found = true;

        registryPet.currentRarity = UpgradeChecker.Rarity.valueOf(pet.get("tier").getAsString());
        registryPet.currentLevel = pet.get("level").getAsJsonObject().get("level").getAsInt();

        final var currentLevel = registryPet.currentLevel;
        final var minimumWantedLevel = registryPet.minimumWantedLevel;

        final var currentRarity = registryPet.currentRarity;
        final var minimumWantedRarity = registryPet.minimumWantedRarity;

        if (currentLevel < minimumWantedLevel || currentRarity.ordinal() < minimumWantedRarity.ordinal()) {
            outputConsumer.accept("Expected [Lvl >= " + minimumWantedLevel + "] " + minimumWantedRarity.name() + " or above rarity " + internalName + ", but has " + "[Lvl " + currentLevel + "] " + currentRarity.name() + ' ' + internalName);
        }

        final var heldItem = pet.get("heldItem");
        final var item = null != heldItem && !(heldItem instanceof JsonNull) ? heldItem.getAsString() : "no pet item";

        final var optimalPetItem = registryPet.optimalPetItem;

        if (null != optimalPetItem && !optimalPetItem.equals(item)) {
            outputConsumer.accept("Expected " + optimalPetItem + " but has " + item + " on " + internalName);
        }
    }

    private static final void processPets(@NotNull final Consumer<String> outputConsumer, @NotNull final JsonArray pets) {
        final var currentPets = new ArrayList<UpgradeChecker.Pet>(16);
        for (final var petElem : pets) {
            final var pet = petElem.getAsJsonObject();
            final var type = pet.get("type").getAsString();
            for (final var registryPet : UpgradeChecker.Pet.allPets) {
                final var internalName = registryPet.internalName;
                if (internalName.equals(type) && UpgradeChecker.checkDependencies(registryPet)) {
                    UpgradeChecker.processPet(outputConsumer, pet, registryPet, internalName);
                    currentPets.add(registryPet);
                }
            }
        }
        for (final var pet : UpgradeChecker.Pet.allPets) {
            if (!currentPets.contains(pet) && UpgradeChecker.checkDependencies(pet)) {
                outputConsumer.accept("Missing " + pet.internalName);
            }
        }
    }

    private static final void processPerks(@NotNull final Consumer<String> outputConsumer, @NotNull final JsonObject perks) {
        for (final var perk : UpgradeChecker.EssenceShopPerk.allPerks) {
            final var value = perks.get(perk.internalName);
            perk.currentLevel = null == value ? 0 : value.getAsInt();

            if (perk.currentLevel != perk.maxLevel && UpgradeChecker.checkDependencies(perk)) {
                outputConsumer.accept("Missing " + perk.descriptionAtLevel.apply("catacombs_boss_luck".equals(perk.internalName) ? perk.currentLevel : perk.maxLevel - perk.currentLevel) + " from " + perk.internalName);
            }
        }
    }

    private static final int findMasterSkullTier(@NotNull final ArrayList<String> rawAccessories) {
        int highestTier = 0;
        for (final var rawAccessory : rawAccessories) {
            if (rawAccessory.startsWith("MASTER_SKULL_TIER_")) {
                final var tier = Utils.safeParseIntFast(StringUtils.removeStart(rawAccessory, "MASTER_SKULL_TIER_"));
                if (tier > highestTier) {
                    highestTier = tier;
                }
            }
        }
        return highestTier;
    }

    @SuppressWarnings("deprecation")
    private static final void processAccessoryOutputFurther(@NotNull final Consumer<String> outputConsumer, @NotNull final String powerStone, final int mp, @NotNull final JsonObject tunings) {
        if (!"fortuitous".equals(powerStone)) {
            outputConsumer.accept("Power Stone set to " + StringUtils.capitalize(powerStone) + " instead of Fortuitous");
        }
        if (1701 > mp) {
            outputConsumer.accept("Magical Power: " + mp + " - less than the max of 1701");
        }
        for (final var stat : tunings.entrySet()) {
            final var name = stat.getKey();
            final var points = stat.getValue().getAsInt();
            if (!"critical_chance".equals(name) && !"walk_speed".equals(name) && 0 < points) {
                outputConsumer.accept("Non CC and Non-Speed tunings (+" + points + " points on " + org.apache.commons.lang3.text.WordUtils.capitalize(StringUtils.replace(name, "_", " ")) + ')');
                break;
            }
        }
    }

    private static final void processAccessoryOutput(@NotNull final Consumer<String> outputConsumer, @NotNull final ArrayList<UpgradeChecker.Accessory> currentAccessories, @NotNull final ArrayList<String> rawAccessories, final int missingCCEnrich, @NotNull final String powerStone, final int mp, @NotNull final JsonObject tunings) {
        if (0 < missingCCEnrich) {
            outputConsumer.accept("Missing +" + missingCCEnrich + " Crit Chance from Enrichment Swapper");
        }
        boolean shouldFindMasterSkullTier = false;
        for (final var accessory : UpgradeChecker.Accessory.allAccessories) {
            if (!currentAccessories.contains(accessory) && UpgradeChecker.checkDependencies(accessory)) {
                if ("MASTER_SKULL_TIER_7".equals(accessory.internalName)) {
                    shouldFindMasterSkullTier = true;
                } else {
                    outputConsumer.accept("Missing " + accessory.internalName + " - " + accessory.description);
                }
            }
        }
        if (shouldFindMasterSkullTier) {
            final var tierBonuses = new int[]{1, 2, 3, 4, 6, 8, 10};
            final var tier = UpgradeChecker.findMasterSkullTier(rawAccessories);
            final var missingBonus = tierBonuses[6] - (0 < tier ? tierBonuses[tier - 1] : 0);
            outputConsumer.accept("Missing +" + missingBonus + "% Strength and Health in MM Catacombs (from Tier " + tier + " to 7)");
        }
        UpgradeChecker.processAccessoryOutputFurther(outputConsumer, powerStone, mp, tunings);
    }

    private static final void processAccessories(@NotNull final Consumer<String> outputConsumer, @NotNull final JsonArray accessories, @NotNull final String powerStone, final int mp, @NotNull final JsonObject tunings) {
        final var rawAccessories = new ArrayList<String>(100);
        final var currentAccessories = new ArrayList<UpgradeChecker.Accessory>(16);
        int missingCCEnrich = 0;
        for (final var accessory : accessories) {
            final var tagElem = accessory.getAsJsonObject().get("tag");
            if (null == tagElem) {
                continue; // Empty accessory bag slot.
            }
            final var tag = tagElem.getAsJsonObject();
            final var attributes = tag.get("ExtraAttributes").getAsJsonObject();
            final var type = attributes.get("id");
            final var enrich = attributes.get("talisman_enrichment");
            if (null != enrich && !"critical_chance".equals(enrich.getAsString())) {
                ++missingCCEnrich;
            }
            if (null != type) {
                final var id = type.getAsString();
                rawAccessories.add(id);
                for (final var registryAccessory : UpgradeChecker.Accessory.allAccessories) {
                    final var internalName = registryAccessory.internalName;
                    if (internalName.equals(id) && UpgradeChecker.checkDependencies(registryAccessory)) {
                        final var lore = UpgradeChecker.getLore(tag);
                        final var loreCondition = registryAccessory.loreCondition;
                        if (null != loreCondition && !UpgradeChecker.loreContains(lore, loreCondition)) {
                            outputConsumer.accept(internalName + " is not " + loreCondition + ", it's " + registryAccessory.loreExtractor.apply(lore));
                        }
                        currentAccessories.add(registryAccessory);
                    }
                }
            }
        }
        UpgradeChecker.processAccessoryOutput(outputConsumer, currentAccessories, rawAccessories, missingCCEnrich, powerStone, mp, tunings);
    }

    private static final void processArrows(@NotNull final Consumer<String> outputConsumer, @NotNull final JsonArray quiver, @NotNull final String selectedArrow) {
        boolean hasArmorshredArrowsLeft = false;
        boolean hasMultipleArrowTypes = false;
        String otherArrowType = "none";
        for (final var arrow : quiver) {
            final var name = arrow.getAsJsonObject().get("display_name");
            if (null != name) {
                final var displayName = name.getAsString();
                if ("Armorshred Arrow".equals(displayName)) {
                    hasArmorshredArrowsLeft = true;
                    break;
                }
                otherArrowType = displayName;
                hasMultipleArrowTypes = true;
            }
        }
        if (!hasArmorshredArrowsLeft) {
            outputConsumer.accept("Missing Armorshred Arrows (has " + otherArrowType + ')');
        } else if (!"Armorshred Arrow".equals(selectedArrow) && hasMultipleArrowTypes) {
            outputConsumer.accept("Armorshred Arrows not selected in Arrow Swapper (Selected: " + selectedArrow + ')');
        }
    }

    private static final void processBank(@NotNull final Consumer<String> outputConsumer, final int bank) {
        if (1_000_000_000 > bank) {
            final var millions = bank / 1_000_000;
            outputConsumer.accept("No 1B Bank (" + millions + "M)");
        }
    }

    private static final void dumpSuggestionsOnProfile(@NotNull final Consumer<String> outputConsumer, @NotNull final JsonObject profile) {
        final var raw = profile.get("raw").getAsJsonObject();
        final var data = profile.get("data").getAsJsonObject();
        final var perks = data.get("perks").getAsJsonObject();
        final var items = data.get("items").getAsJsonObject();
        final var accessories = items.get("accessory_bag").getAsJsonArray();
        final var rawAccessories = raw.get("accessory_bag_storage").getAsJsonObject();
        final var pets = data.get("pets").getAsJsonObject().get("pets").getAsJsonArray();
        UpgradeChecker.processPets(outputConsumer, pets);
        UpgradeChecker.processPerks(outputConsumer, perks);
        UpgradeChecker.processAccessories(outputConsumer, accessories, rawAccessories.get("selected_power").getAsString(), rawAccessories.get("highest_magical_power").getAsInt(), rawAccessories.get("tuning").getAsJsonObject().get("slot_0").getAsJsonObject());
        final var favoriteArrow = data.get("misc").getAsJsonObject().get("uncategorized").getAsJsonObject().get("favorite_arrow");
        UpgradeChecker.processArrows(outputConsumer, items.get("quiver").getAsJsonArray(), null == favoriteArrow ? "none" : favoriteArrow.getAsJsonObject().get("formatted").getAsString());
        UpgradeChecker.processBank(outputConsumer, data.get("currencies").getAsJsonObject().get("bank").getAsInt());
    }

    static final void dumpSuggestions(@NotNull final String playerName, @NotNull final Consumer<String> outputConsumer) {
        UpgradeChecker.upgradeCheckerExecutor.execute(() -> {
            outputConsumer.accept("Fetching data from API, please wait...");
            Utils.sendWebRequest("https://sky.shiiyu.moe/stats/" + playerName, "text/html", false, 30L); // The api doesn't update unless a request to frontend is sent first.
            final var output = Utils.sendWebRequest("https://sky.shiiyu.moe/api/v2/profile/" + playerName + "?cache=" + UUID.randomUUID(), "application/json", true, 10L); // The api returns cached a result (the cache seems to not expire even after a day) so we use a query parameter with random UUID to bypass the cache.
            if (null == output) {
                outputConsumer.accept("An error occurred while connecting to the API.");
                return;
            }
            final var profiles = Utils.parseJsonObjectFromString(output).get("profiles").getAsJsonObject();
            for (final var profile : profiles.entrySet()) {
                final var profileElement = profile.getValue().getAsJsonObject();
                final var current = profileElement.get("current").getAsBoolean();
                if (current) {
                    UpgradeChecker.dumpSuggestionsOnProfile(outputConsumer, profileElement);
                    break;
                }
            }
            outputConsumer.accept("See above for upgrade suggestions. If theres nothing above, everything is good!");
        });
    }
}
