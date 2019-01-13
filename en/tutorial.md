# Tutorial

This part is created for newcomers who knows nothing about EpicBanItem in order to help them get started quickly.

## Plugin Installation

Please download the [latest recommended version](https://ore.spongepowered.org/EpicBanItem/EpicBanItem/versions/recommended/download) on the Ore distribution platform, and put the downloaded plugin file into `mods/` folder. There are not any other ways provided officially to download EpicBanItem.

**The server should fit one of two requirements so that EpicBanItem is able to work normally**:

* **Based on SpongeVanilla whose version is no lower than `1.12.2-7.1.0-BETA-98`**.
* **Based on SpongeForge whose version is no lower than `1.12.2-2705-7.1.0-BETA-3361`**.

However, we recommend installing the latest version of either SpongeVanilla or SpongeForge even though EpicBanItem itself should work on any version which is no lower than the versions shown above. The reason is that there may be some BUGs caused by other plugins or Sponge itself, which may interfere with EpicBanItem.

## Considering Crafting Recipes

Disabling the crafting recipes leading to an item is usually a common need, while it is widely known that crafting recipes are not allowed to be hot reloaded in Forge servers under Minecraft 1.12, so it is impossible to let mods or plugins allow those recipes hot reloaded in normal ways. However, considering that EpicBanItem itself is a plugin which supports hot reloading, we suggest three solutions sorted in descending order of recommendation:

1. Modifying crafting recipes by other plugins or mods, and [CraftTweaker](https://minecraft.curseforge.com/projects/crafttweaker) is considered as the best. Using CraftTweaker is considered as the best solution for mod compatibility, although CraftTweaker itself does not support hot reloading at all.

2. Adding a mod named [CraftingRecipeRedirector](https://github.com/ustc-zzzz/CraftingRecipeRedirector/releases) to server and using EpicBanItem. The developer team has received a lot of requests that hopes EpicBanItem support modifying crafting recipes, therefore an additional mod is released and depended on in order to support hot reloading. This way is not officially supported by Forge, so stability cannot be guaranteed, but this is a compromise.

3. Using EpicBanItem directly and depending on it only. This solutions should only be applied to SpongeVanilla servers, because Sponge only supports the original workbench, while other mods are not considered to be supported.

## Making the Items Banned

> TBD

## Editing the Rules

> TBD

## Summary

> TBD
