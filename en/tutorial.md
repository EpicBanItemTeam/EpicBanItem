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

Now we get started by restricted use of a wooden axe. As an example, let's assume that the player `zzzz_ustc` is the server admin.

Just enter the game and execute the command at the chat window (please ensure that `zzzz_ustc` has the `minecraft.command.give` permission):

```mcfunction
give zzzz_ustc minecraft:wooden_axe
```

Now there should be a wooden axe in your hand (if not, just switch to the state in which you are holding a wooden axe in your hand). Then we execute the following command at the chat window (please ensure that `zzzz_ustc` has the `epicbanitem.command.create` permission):

```mcfunction
ebi create ban-wooden-axe
```

It infers that a rule named `ban-wooden-axe` will be added, which restricts the use of all the items with the same item type of the one in your hand (in this example it is wooden axes, which type is `minecraft:wooden_axe`).

The server admin can specify the name of a rule as they want, but the name can only be constructed by these characters shown below (non-ascii characters are not allowed):

* Lower case latin letters (`abcdefghijklmnopqrstuvwxyz`)
* Digits (`0123456789`)
* Underlines (`_`)
* Dashes (`-`)

If everything works well, an message will be poped out:

> Successfully added the rule: ban-wooden-axe

Now you are able to use the following commands for listing all the rules avaliable or all the rules for the items which type is `minecraft:wooden_axe` (please ensure that `zzzz_ustc` has the `epicbanitem.command.list` permission):

```mcfunction
ebi list
ebi list minecraft:wooden_axe
```

Here is a possible output:

> --------------------- List of rules ----------------------  
> ban-wooden-axe  minecraft:wooden_axe  
> --------------------------- « » ----------------------------  

## Editing the Rules

> TBD

## Summary

> TBD
