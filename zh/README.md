# EpicBanItem

一个基于 NBT 的物品禁用插件。

![EpicBanItem.png](https://forums-cdn.spongepowered.org/uploads/default/original/3X/d/f/df777d2f56331853a78fafc6876c59a412a2353d.png)

主要开发者：

* yinyangshi（[@euOnmyoji](https://github.com/euOnmyoji)）

协助开发者：

* GiNYAi（[@ginyai](https://github.com/ginyai)）
* ustc_zzzz（[@ustc-zzzz](https://github.com/ustc-zzzz)）

EpicBanItem 目前适用于 Minecraft 1.12.2 和 SpongeAPI 7.1.0。

EpicBanItem 通过将物品（有时还包括方块）映射到 NBT，并对其进行检查和更新，以完成物品禁用的目标。物品映射到的 NBT 格式和 Minecraft 中的[命令 NBT 标签](https://minecraft.gamepedia.com/Tutorials/Command_NBT_tags#Items)格式，而非和 Sponge 提供的一致。唯一的区别在于，生成 NBT 时，`Count` 标签会被抹除，因此只会剩下 `id` 和 `Damage` 两个标签，有时还会有 `tag` 标签。

EpicBanItem 使用类似于 [MongoDB](https://docs.mongodb.com/manual/) 的方式[检索](https://docs.mongodb.com/manual/tutorial/query-documents/)和[更新](https://docs.mongodb.com/manual/tutorial/update-documents/) NBT。EpicBanItem 目前已经支持了 MongoDB 中大多数用于检索和更新的[运算符](https://docs.mongodb.com/manual/reference/operator/)。

和 EpicBanItem 有关的配置文件位于 `config/epicbanitem/` 目录下，其中包含 `banitem.conf`（用于存储规则及相关选项）和 `settings.conf`（用于存储全局配置）两个文件。所有和 EpicBanItem 的命令均以 `/ebi` 开头。所有和 EpicBanItem 有关的权限均以 `epicbanitem`。

EpicBanItem 整体[使用 GPL-3.0 协议授权](LICENSE)，其中包含了[使用 LGPL-3.0 协议授权](https://github.com/Bastian/bStats-Metrics/blob/master/LICENSE)的 `bstats-metrics`，用于数据收集和统计。使用者可自行编辑名为 `config/bStats/config.conf` 的文件选择是否开启数据收集和统计。
