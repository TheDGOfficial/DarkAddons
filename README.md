# DarkAddons

DarkAddons is a mod focused around Hypixel Skyblock, providing Quality of Life features/enchancements, along with other general performance enchancing features.

# Note

This mod is mainly created for personal use by me and is currently not released in any mainstream platform like Modrinth or CurseForge and does not yet have any official Discord server for support and/or communication. If you want to go ahead to using it you can still do so by the [GitHub Releases](https://github.com/TheDGOfficial/DarkAddons/releases) section, but keep in mind that support might be limited and features that myself use will be prioritized in development.

# Dependencies

DarkAddons currently requires the Skytils mod as a dependency to function, your game will likely crash without it. This due to keeping the JAR size small, deduplicating classes and not having to reinvent the wheel. The mod uses Utils classes, APIs and libraries bundled by Skytils for the reasons mentioned earlier.

If you crash from NoSuchFieldError or a Mixin error - ensure you have latest Essential (type /essential, enable auto update and restart your game). This because we use the latest ElementaVersion. 

For Mixin errors, most common culprit is using outdated NEU. Get NEU from [here](https://github.com/NotEnoughUpdates/NotEnoughUpdates/). This because DarkAddons when NEU is present (so NEU is not required, but if you have it, you must have a recent enough version to be compatible) applies some Mixins for optimization purposes, but it does not check the specific NEU version before applying.

If you want an even more updated NEU version with memory leak fix, attribute shard in dungeon chest profit support and more FPS, get NEU from [here](https://github.com/TheDGOfficial/NotEnoughUpdates/releases), from my fork.

# Features

For higlighted features of the mod with some screenshots, click [here](https://github.com/TheDGOfficial/DarkAddons/blob/main/HIGHLIGHTED_FEATURES.md). Keep in mind this list is not exhaustive and will only show higlighted features. You can access the feature toggles by typing /darkaddons and navigating the config. The HUD elements are also edited from the same command. If you have any questions, create a [Discussion](https://github.com/TheDGOfficial/DarkAddons/discussions) or an [Issue](https://github.com/TheDGOfficial/DarkAddons/issues).
