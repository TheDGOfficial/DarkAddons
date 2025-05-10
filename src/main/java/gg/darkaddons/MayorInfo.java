package gg.darkaddons;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;

import net.minecraft.inventory.Slot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MayorInfo {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private MayorInfo() {
        super();

        throw Utils.staticClassException();
    }

    @NotNull
    private static final String API_URL = "https://api.hypixel.net/resources/skyblock/election";

    @NotNull
    private static final String KNOWN_MAYORS_URL = "https://raw.githubusercontent.com/Skytils/SkytilsMod-Data/refs/heads/main/constants/mayors.json";

    @NotNull
    private static final HashSet<MayorInfo.Mayor> allKnownMayors = new HashSet<>(16);

    @Nullable
    private static String currentMayor;

    @Nullable
    private static String currentJerryPerkpocalypseMayor;

    @NotNull
    private static final Matcher jerryNextPerkRegexMatcher = Pattern.compile("§7Next set of perks in §e(?<h>\\d+?)h (?<m>\\d+?)m").matcher("");

    @NotNull
    private static final HashSet<String> allPerks = new HashSet<>(4);

    @NotNull
    private static final ExecutorService mayorFetcherThread = Executors.newSingleThreadExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons Mayor Fetcher Thread"));

    static final void init() {
        MayorInfo.mayorFetcherThread.execute(MayorInfo::loadKnownMayors);

        DarkAddons.registerTickTask("fetch_and_assign_current_mayor_and_perks", 60 * 20, true, () -> {
            if (!DarkAddons.isInSkyblock() || SkyblockDetection.isInAlphaNetwork()) {
                return;
            }

            MayorInfo.mayorFetcherThread.execute(MayorInfo::assignCurrentMayor);
        });
    }

    private static final void assignCurrentMayor() {
        final var response = Utils.sendWebRequest(MayorInfo.API_URL, "application/json", false, 10L);
        if (null != response) {
            final var json = Utils.parseJsonObjectFromString(response);
            final var success = json.get("success").getAsBoolean();
            if (success) {
                final var mayor = json.get("mayor").getAsJsonObject();
                MayorInfo.currentMayor = mayor.get("name").getAsString();

                MayorInfo.allPerks.clear();

                for (final var perk : mayor.getAsJsonArray("perks")) {
                    MayorInfo.allPerks.add(perk.getAsJsonObject().get("name").getAsString());
                }

                if (mayor.has("minister")) {
                    MayorInfo.allPerks.add(mayor.getAsJsonObject("minister").getAsJsonObject("perk").get("name").getAsString());
                }
            }
        }
    }

    @Nullable
    static final String getCurrentMayor() {
        return MayorInfo.currentMayor;
    }

    @NotNull
    static final HashSet<String> getAllPerks() {
        return MayorInfo.allPerks;
    }

    private static final void loadKnownMayors() {
        final var response = Utils.sendWebRequest(MayorInfo.KNOWN_MAYORS_URL, "application/json", false, 10L);
        if (null != response) {
            final var json = Utils.parseJsonFromString(response);
            final var result = new ArrayList<MayorInfo.Mayor>(16);

            for (final var mayorElement : json.getAsJsonArray()) {
                final var mayorObject = mayorElement.getAsJsonObject();

                final var name = mayorObject.getAsJsonPrimitive("name").getAsString();
                final var perksArray = mayorObject.getAsJsonArray("perks");

                final var perkNames = new HashSet<String>(4);

                for (final var perkElement : perksArray) {
                    final var perkObj = perkElement.getAsJsonObject();
                    final var perkName = perkObj.getAsJsonPrimitive("name").getAsString();

                    perkNames.add(perkName);
                }

                result.add(new MayorInfo.Mayor(name, perkNames));
            }

            MayorInfo.allKnownMayors.clear();
            MayorInfo.allKnownMayors.addAll(result);
        }
    }

    private static final class Mayor {
        @NotNull
        private final String name;
        @NotNull
        private final HashSet<String> perks;

        private Mayor(@NotNull final String name, @NotNull final HashSet<String> perks) {
            super();

            this.name = name;
            this.perks = perks;
        }

        @Override
        public final String toString() {
            return "Mayor{" +
                "name='" + this.name + '\'' +
                ", perks=" + this.perks +
                '}';
        }
    }

    static final void onDrawSlot(@NotNull final GuiContainer gui, @NotNull final Slot slot) {
        if (!DarkAddons.isInSkyblock() || SkyblockDetection.isInAlphaNetwork()) {
            return;
        }

        if (gui.inventorySlots instanceof final ContainerChest containerChest) {
            final var chestName = containerChest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            final var slotNumber = slot.slotNumber;
            final var stack = slot.getStack();

            if (null != stack && ("Mayor Jerry".equals(chestName) && (13 == slotNumber || "§dJERRY IS MAYOR!!!".equals(stack.getDisplayName())) || "Calendar and Events".equals(chestName) && 37 == slotNumber)) {
                final var lore = Utils.getItemLore(stack);
                if (!lore.contains("§9Perkpocalypse Perks:")) {
                    return;
                }

                String endingIn = null;
                final var loreLength = lore.size();
                final var iterator = lore.listIterator(loreLength);
                while (iterator.hasPrevious()) {
                    final var line = iterator.previous();
                    if (line.startsWith("§7Next set of perks in")) {
                        endingIn = line;
                        break;
                    }
                }
                if (null == endingIn) {
                    return;
                }

                final var perks = new ArrayList<String>(loreLength);

                for (var i = lore.indexOf("§9Perkpocalypse Perks:") + 1; i < loreLength - 1; ++i) {
                    final var line = lore.get(i);

                    if (line.startsWith("§b")) {
                        perks.add(Utils.removeControlCodes(line));
                    }
                }

                if (perks.isEmpty()) {
                    return;
                }

                final var mayor = MayorInfo.findMayorWithPerks(perks);

                if (null == mayor) {
                    return;
                }

                if (!MayorInfo.jerryNextPerkRegexMatcher.reset(endingIn).find()) {
                    return;
                }

                MayorInfo.currentJerryPerkpocalypseMayor = mayor.name;
            }
        }
    }

    @Nullable
    private static final MayorInfo.Mayor findMayorWithPerks(@NotNull final ArrayList<String> perks) {
        MayorInfo.Mayor mayor = null;

        for (final var knownMayor : MayorInfo.allKnownMayors) {
            for (final var perk : knownMayor.perks) {
                if (perks.contains(perk)) {
                    mayor = knownMayor;
                    break;
                }
            }

            if (null != mayor) {
                return mayor;
            }
        }

        return null;
    }

    @Nullable
    static final String getJerryMayor() {
        return MayorInfo.currentJerryPerkpocalypseMayor;
    }
}
