# EpicBanItem

A banitem with nbt plugin in sponge.

* Getting started guide: <https://euonmyoji.github.io/EpicBanItem---Sponge/#/en/tutorial>
* Ore distribution platform: <https://ore.spongepowered.org/EpicBanItem/EpicBanItem>
* Plugin documentation: <https://euonmyoji.github.io/EpicBanItem---Sponge/#/en/>
* bStats: <https://bstats.org/plugin/sponge/EpicBanItem>

一个基于 NBT 的物品禁用插件。

* 入门教程：<https://euonmyoji.github.io/EpicBanItem---Sponge/#/zh/tutorial>
* Ore 发布平台：<https://ore.spongepowered.org/EpicBanItem/EpicBanItem>
* 插件文档：<https://euonmyoji.github.io/EpicBanItem---Sponge/#/zh/>
* bStats：<https://bstats.org/plugin/sponge/EpicBanItem>

![EpicBanItem.png](https://forums-cdn.spongepowered.org/uploads/default/original/3X/d/f/df777d2f56331853a78fafc6876c59a412a2353d.png)

## English

Main developer:

* yinyangshi ([@euOnmyoji](https://github.com/euOnmyoji))

Assist developers:

* GiNYAi ([@ginyai](https://github.com/ginyai))
* ustc_zzzz ([@ustc-zzzz](https://github.com/ustc-zzzz))

EpicBanItem is now based on Minecraft 1.12.2 and SpongeAPI 7.1.0.

EpicBanItem achieves the goal of item banning by mapping items (and blocks sometimes) to NBT and then checking and updating them. The NBT format mapped to is the same as [Command NBT Tags](https://minecraft.gamepedia.com/Tutorials/Command_NBT_tags#Items) format in Minecraft, instead of the one provided by Sponge. The only difference is, the `Count` tag will be erased when generating NBT, and only two tags named `id` and `Damage` will exist, sometimes togethered with `tag`.

EpicBanItem uses the method similar to [MongoDB](https://docs.mongodb.com/manual/) for [querying](https://docs.mongodb.com/manual/tutorial/query-documents/) and [updating](https://docs.mongodb.com/manual/tutorial/update-documents/) NBT. EpicBanItem currently supports most of the [operators](https://docs.mongodb.com/manual/reference/operator/) used in MongoDB for querying and updating.

All the configurations related to EpicBanItem are located in `config/epicbanitem/` directory, which contains two files named `banitem.conf` (used to store rules and related options) and `settings.conf` (used to store global options). All the commands related to EpicBanItem begin with `/ebi`. All the permissions related to EpicBanItem begin with `epicbanitem`.

EpicBanItem is entirely [licensed under GPL-3.0](LICENSE) and includes `bstats-metrics` [licensed under LGPL-3.0](https://github.com/Bastian/bStats-Metrics/blob/master/LICENSE) for data collection and statistics. The file named `config/bStats/config.conf` can be edited in order to choose whether to enable data collection and statistics or not.

## 简体中文

主要开发者：

* yinyangshi（[@euOnmyoji](https://github.com/euOnmyoji)）

协助开发者：

* GiNYAi（[@ginyai](https://github.com/ginyai)）
* ustc_zzzz（[@ustc-zzzz](https://github.com/ustc-zzzz)）

EpicBanItem 目前适用于 Minecraft 1.12.2 和 SpongeAPI 7.1.0。

EpicBanItem 通过将物品（有时还包括方块）映射到 NBT，并对其进行检查和更新，以完成物品禁用的目标。物品映射到的 NBT 格式和 Minecraft 中的[命令 NBT 标签](https://minecraft.gamepedia.com/Tutorials/Command_NBT_tags#Items)格式，而非和 Sponge 提供的一致。唯一的区别在于，生成 NBT 时，`Count` 标签会被抹除，因此只会剩下 `id` 和 `Damage` 两个标签，有时还会有 `tag` 标签。

EpicBanItem 使用类似于 [MongoDB](https://docs.mongodb.com/manual/) 的方式[检索](https://docs.mongodb.com/manual/tutorial/query-documents/)和[更新](https://docs.mongodb.com/manual/tutorial/update-documents/) NBT。EpicBanItem 目前已经支持了 MongoDB 中大多数用于检索和更新的[运算符](https://docs.mongodb.com/manual/reference/operator/)。

和 EpicBanItem 有关的配置文件位于 `config/epicbanitem/` 目录下，其中包含 `banitem.conf`（用于存储规则及相关选项）和 `settings.conf`（用于存储全局配置）两个文件。所有和 EpicBanItem 的命令均以 `/ebi` 开头。所有和 EpicBanItem 有关的权限均以 `epicbanitem` 开头。

EpicBanItem 整体[使用 GPL-3.0 协议授权](LICENSE)，其中包含了[使用 LGPL-3.0 协议授权](https://github.com/Bastian/bStats-Metrics/blob/master/LICENSE)的 `bstats-metrics`，用于数据收集和统计。使用者可自行编辑名为 `config/bStats/config.conf` 的文件选择是否开启数据收集和统计。
