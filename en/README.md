# EpicBanItem

The sponge plugin for item restriction by checking nbt tags.

* Plugin documentation: <https://docs.ebi.team/#/en/>
* Getting started guide: <https://docs.ebi.team/#/en/tutorial>
* Ore platform: <https://ore.spongepowered.org/EpicBanItem/EpicBanItem>
* bStats metrics of plugin: <https://bstats.org/plugin/sponge/EpicBanItem>

![EpicBanItem1](https://forums-cdn.spongepowered.org/uploads/default/original/3X/c/d/cdda679f41f9665b90a7f7703c01e88dec3e30a9.png)

![EpicBanItem2](https://forums-cdn.spongepowered.org/uploads/default/original/3X/b/7/b7b3cd35681af738ddc510624e92738460e633fc.png)

![EpicBanItem3](https://forums-cdn.spongepowered.org/uploads/default/original/3X/3/b/3be48a12c964ec6f418f333d14a1dc2dcd2e0bd8.png)

Main developer:

* yinyangshi ([@euOnmyoji](https://github.com/euOnmyoji))

Assist developers:

* GiNYAi ([@ginyai](https://github.com/ginyai))
* ustc_zzzz ([@ustc-zzzz](https://github.com/ustc-zzzz))
* SettingDust ([@SettingDust](https://github.com/SettingDust))

EpicBanItem is now based on Minecraft 1.12.2 and SpongeAPI 7.1.0.

EpicBanItem achieves the goal of item banning by mapping items (and blocks sometimes) to NBT and then checking and updating them. The NBT format mapped to is the same as [Command NBT Tags](https://minecraft.gamepedia.com/Tutorials/Command_NBT_tags#Items) format in Minecraft, instead of the one provided by Sponge. The only difference is, the `Count` tag will be erased when generating NBT, and only two tags named `id` and `Damage` will exist, sometimes togethered with `tag`.

EpicBanItem uses the method similar to [MongoDB](https://docs.mongodb.com/manual/) for [querying](https://docs.mongodb.com/manual/tutorial/query-documents/) and [updating](https://docs.mongodb.com/manual/tutorial/update-documents/) NBT. EpicBanItem currently supports most of the [operators](https://docs.mongodb.com/manual/reference/operator/) used in MongoDB for querying and updating.

All the configurations related to EpicBanItem are located in `config/epicbanitem/` directory, which contains two files named `banitem.conf` (used to store rules and related options) and `settings.conf` (used to store global options). All the commands related to EpicBanItem begin with `/ebi`. All the permissions related to EpicBanItem begin with `epicbanitem`.

EpicBanItem is entirely [licensed under GPL-3.0](LICENSE) and includes `bstats-metrics` [licensed under LGPL-3.0](https://github.com/Bastian/bStats-Metrics/blob/master/LICENSE) for data collection and statistics. The file named `config/bStats/config.conf` can be edited in order to choose whether to enable data collection and statistics or not.
