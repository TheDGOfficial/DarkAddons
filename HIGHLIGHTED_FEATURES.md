# Higlighted features of the DarkAddons mod

Keep in mind this list is not exhaustive, only shows features worth higlighting, and, it might lack the niche features or newly added features.

# Class Average 50 Display

A class average 50 runs needed display in-game. It updates automatically as you do runs, e.g., after finishing 1 mage run the runs needed for the mage class will go down by 1. This not something static; it dynamically recalculates your way to the class average 50 after gaining class XP (so you don't even need to finish a run, XP from failed run also triggers an update).

Compared to alternatives such as SoopyV2's Class Average 50 calculator, it has the following advantages:
 - Live updating display, right in your screen, as said earlier.
 - Soopy's does not take into account +10% Class XP from Essence Shop's (only supports the Mage one), while DA's support.
 - DA has other extensions like Derpy support, daily +50% XP support, along with custozamability of the HUD compactness, shadow, size.
 - The calculation runs on your machine and the Class XPs are fetched 1 time from API at game startup and then the mod detects class XP gain from chat messages, so that no further Hypixel API calls are necessary. This means since all the calculations are done without a HTTP network connection, both the display and the RTCA command work blazingly fast, compared to the Soopy's.

The display shows your own progress to CA 50, while the RTCA command allows you to show other's progress, see below.

Here's an image I took shortly before reaching CA 50 and this feature helped me immensely to stay motivated, seeing the counter go down 1 by 1 after every run gave me more motivation:

![Class Average 50 Display preview](https://github.com/user-attachments/assets/a6ab4608-95f9-47aa-888c-fd48d3f23feb)

# RTCA Command

A command to see you (however use display since its better suited for yourself) and others progress to Class Average 50.

Here's the RTCA of a friend of mine that i took at the time of developing the feature:

![RTCA command output preview](https://github.com/user-attachments/assets/da49e64b-38bd-4145-b30e-fddcedb6b24d)

# Unopened Chests Display

A simple feature that shows unopened chests at croesus on your screen.

![Unopened Chests Display preview](https://github.com/user-attachments/assets/7a2ae509-cbe1-4c1a-a689-23433eff9b7e)

# Show Maxor Health Percentage

Shows Maxor's health right on your screen. Since the vanilla bossbar disappear at times, this display can help you a lot when playing Archer to not overdps and fail enrage skip. Even when your vanilla bossbar disappears the displayed HP value will be correct and updated live because we grab the HP directly from the wither entity instead of the bossbar; this means unless you unload the chunk the Maxor is in, the shown HP value will be correct.

![Show Maxor Health percentage preview](https://github.com/user-attachments/assets/bc687c6e-2979-46cf-abd5-a2680a85304b)

# Blessing Display

Shows Blessing tiers with color codes for each level range in your screen.

![Blessing Display preview](https://github.com/user-attachments/assets/bea24f34-fd9e-495f-b7c2-c4d95ca8bf17)

# EHP on Screen

Shows your live EHP on screen calculated on your current HP (not max HP). Includes support for absorption hearts. Shows the EHP change after taking damage or casting a healing ability inside the paranthesis. It will show different colors based on certain EHP thresholds.

![EHP on Screen preview](https://github.com/user-attachments/assets/5c7449a4-6a45-4fa2-abed-6a89ea12f8b5)

# Rogue Sword Timer on Screen

Shows timer for time left on Rogue Sword's speed boost on your screen.

![Rogue Sword Timer on Screen preview](https://github.com/user-attachments/assets/87da26ad-8be2-44b6-99fa-dc742194a105)

# M7 Dragon HUD

Shows M7 Dragons and their HP on screen. Also shows spawn timer when they are spawning. Shows R when they are at their statue. Shows a checkmark or a X icon depending on if the statue of that dragon has been successfully destroyed or not.

![M7 Dragon HUD preview](https://github.com/user-attachments/assets/f0ef9682-491d-432b-9b96-0f315a6c68e6)

**Note:** This the screenshot from the "Edit Locations" menu and the contents are demo text, thats why theres an outline background.

# Chroma Scoreboard

Makes the SKYBLOCK header in your scoreboard (sidebar) chroma color. Requires SkyblockAddons or SkyblockAddons Unofficial to be installed for the Chroma color support. This because SBA has the smoothest animated chroma out of all mods that add chroma.

![Chroma Scoreboard preview](https://github.com/user-attachments/assets/0056e0e2-2751-43b5-889e-301fc77576f5)

# Golden Fish Timer

Shows an accurate Golden Fish Timer, along with helper information about how much time you have till you need to throw a rod again to be counting as fishing (have able to spawn a golden fish) and once you spawn a golden fish it will display a timer for when the golden fish will despawn, resetting in each interaction with it.
 Note: We added this feature before SkyHanni added a very similar one. We're going to keep maintaining and updating it, they are about functionally equiavalant but our's is slightly more performant.

![Golden Fish Timer preview](https://github.com/user-attachments/assets/d1ee0598-bf37-48ef-a2dc-840c0c02f20d)

# Blaze Effect Timer

Shows time left on your Smoldering Polarization or Wisp's Ice Flavored Splash Potion on your screen.

![Blaze Effect Timer preview](https://github.com/user-attachments/assets/34d88c7a-60ba-4f96-a26b-54bd697697b6)

# FPS Limit Display

Useful if you cap your FPS to 30 to save power when you go AFK, but sometimes forget to set it back to unlimited for smooth gameplay when you come back from AFK. This Display will show nothing if your FPS is uncapped but will show the FPS cap if your FPS is capped in any way.

![FPS Limit Display preview](https://github.com/user-attachments/assets/2f509406-5471-4c1d-82d8-57510629de70)

# FPS Display

Shows your FPS (amount of frames rendered last second) on your screen. This is not an average FPS. It's the live FPS. Accurate to the nanosecond precision. Colored by FPS thresholds to show red for low, yellow for suboptimal and green for ok.

![FPS Display preview](https://github.com/user-attachments/assets/2db04c75-4132-4f37-b167-60792389dc5b)

# TPS Display

Shows server's TPS (the amount of ticks server has completed in the last second) on your screen. This not the average TPS. It's the live TPS. Colored by TPS thresholds to show red for bad, yellow for bit laggy and green for decent.

![TPS Display preview](https://github.com/user-attachments/assets/9e50b7cc-96c9-4e47-ba15-1df0628a5e1a)

# Ping Display

Shows your ping to the server, updated every second. Colored by Ping thresholds to show red for poor ping, yellow for ok ping, green for good ping.

![Ping Display preview](https://github.com/user-attachments/assets/1c42f5c6-ae98-47eb-9cd7-a6a6fa941292)

# Armor Stand Optimizer

A feature that can potentially triple your FPS on maps loaded with armor stands. My island for example, has maximum furniture amount placed and has more than 600 armor stands. This causes a massive FPS drop and I get 30-40 FPS or so without Armor Stand Optimizer. After enabling it, the FPS jumps to almost 100.

Disabled:
![Armor Stand Optimizer disabled preview](https://github.com/user-attachments/assets/20f5cdbb-c040-42fb-beb4-549ff21bb5b2)

Enabled:
![Armor Stand Optimizer enabled preview](https://github.com/user-attachments/assets/5239c759-5d79-4c91-8fe7-78b6dbd28d1b)

Some of the cakes are hidden because they count as armor stands and are far enough from player. You can configure the limit. By default it will only render 50 armor stands closest to the player. So the cakes will be visible when you walk closer to them.

We added this feature before SkyHanni added "Hide Far Entities" feature, but theirs hides all entities and might be problematic. Ours only focuses on the entity actually eating performance alive, which is Armor Stands.

# Slayer RNG Display

Shows live updated odds to drop your selected RNG while doing slayers, your money per hour, average boss kill time, magic find, meter progress and more right on your screen! Dynamically refreshed everytime you kill a slayer boss!

![Slayer RNG Display preview](https://github.com/user-attachments/assets/c17145ed-8ccd-4084-828d-4cf1622bc00b)

# Installer

DarkAddons provides an installer opened by double clicking just like the Forge, Optifine and SBA mods!

This provides ease-of-use and simplicity to start using the mod, rather than manually putting it to your mods folder, which also requires you to locate the .minecraft folder in AppData, which might not be as straightfoward as is to us, for the non-technical people.

You might be used to see the installer only being present on big mods, but it's actually easy to implement, and we have it for you.

Double-click to the JAR you have downloaded on our [Releases](https://github.com/TheDGOfficial/DarkAddons/releases) page to open the installer and start installing the mod!

![Installer preview](https://github.com/user-attachments/assets/f2cfe083-15fe-4151-b20c-1d9c4e3b8da1)

# Mixins

DarkAddons uses Mixins to hook into vanilla game code and even other mod's code, to provide bug fixes, performance enchancements, or implement new features based around them.

This is better than using say, ASM Transformers for compatibility with other mods because of the nature of the Mixin subsystem.

Check out the [Mixin folder](https://github.com/TheDGOfficial/DarkAddons/tree/main/src/main/java/gg/darkaddons/mixins) to see all our Mixins!

As the end user there is no command or feature you can use to enable, disable or configure Mixins directly at the moment, but the regular feature toggles enable or disable the Mixin triggers, while not disabling the Mixin's itself.

# Profiler

DarkAddons bundles a self-written sampling Profiler written in Java with it. This is less advanced and less accurate than the equiavalants of Spark, but it's also more lightweight and simple to use. The results rather than being uploaded to a remote server, will be displayed on your local machine, with a simple HTML code allowing you to see full stack traces or just the method names.

The results are automatically remapped from names along the likes of func_149324_c to the real names like getPlayerName. This is done with the provided mappings file inside the mod's JAR which you can see [here](https://github.com/TheDGOfficial/DarkAddons/blob/main/src/main/resources/methods.v2.csv). This CSV file holds all the mappings and it's even more extended than the default 1.8.9 mappings because we took some mappings from 1.9.4 that were also applying cleanly to the 1.8.9 names, for the methods that had no mapping available previously, like most of the methods in the JsonToNBT class in the vanilla code.

Summary of top time taking methods on a sub 1 minute profiler run with all default settings:
![Profiler preview](https://github.com/user-attachments/assets/98bc40eb-7a7c-44f0-aae5-e082bcb134eb)

Extending to see the stack trace of the first element:
![Profiler preview extended](https://github.com/user-attachments/assets/0d557404-dd5f-48cc-ac8e-fe8e10a5a48e)

To use the profiler, type /darkaddon startprofiling 100. The 100 is the sampling interval in milliseconds. To see the results and stop the profiler, you can type /darkaddon stopprofiling. You can then open localhost:8000 in your browser to see and navigate the results. The page is simple HTML and is stored locally so it will load instantly, the mod will close the temporary dummy web server created to serve the page to you after the page is served. This to ensure no memory leaks are being created by keeping the web server alive in the background. For this reason, you will lose the profiler results if you refresh the page, unless you have started and stopped another profiler before doing so.

# And more!

Check out all the features by typing /darkaddons and navigating the config when you install the mod! You can also look at the source code of all indiviual features over here at GitHub.
