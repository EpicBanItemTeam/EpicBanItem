# 入门教程

这部分内容针对还对 EpicBanItem 一无所知的新手，用于帮助他们快速上手这一插件。

## 安装插件

去 Ore 发布平台下载[最新推荐版本](https://ore.spongepowered.org/EpicBanItem/EpicBanItem/versions/recommended/download)，并将下载得到的插件放置到 `mods/` 目录下。EpicBanItem 目前不提供任何其他的官方下载方式。

**为保证 EpicBanItem 正常工作，你的服务器需要以下两点条件之一**：

* **使用不低于 `1.12.2-7.1.0-BETA-98` 的 SpongeVanilla 版本**。
* **使用不低于 `1.12.2-2705-7.1.0-BETA-3361` 的 SpongeForge 版本**。

不过，即使 EpicBanItem 本身理应能在不低于这两个版本的任何版本上工作，我们也建议服务器安装 SpongeVanilla 或 SpongeForge 的最新版本。这是因为其他插件或 Sponge 本身的 BUG 也可能干扰 EpicBanItem 的正常工作。

## 禁用物品

我们从禁用一个木斧开始。作为示例，我们假设玩家 `zzzz_ustc` 是服务器管理员。

登录游戏，在聊天界面执行命令（请保证 `zzzz_ustc` 拥有 `minecraft.command.give` 权限）：

```mcfunction
give zzzz_ustc minecraft:wooden_axe
```

现在你的手里大概率已经拿着一个木斧了（如果没有的话，请切换到拿着一个木斧的状态）。我们在聊天界面执行命令以禁用这个木斧（请保证 `zzzz_ustc` 拥有 `epicbanitem.command.create` 权限）：

```mcfunction
ebi create ban-wooden-axe
```

这条命令的意思是：添加一个名为 `ban-wooden-axe` 的规则，该规则禁用所有和你当前手上拿着的物品类型相同的物品（这里的话也就是木斧，`minecraft:wooden_axe` 这一物品）。

服务器管理员可以任意指定规则名称，但规则名称只能由以下几种字符组成，**不支持中文等非 ascii 字符**：

* 小写英文字母（`abcdefghijklmnopqrstuvwxyz`）
* 数字（`0123456789`）
* 短横线（`-`）
* 下划线（`_`）

如果添加成功，那么聊天界面应该会弹出一句提示：

> 成功添加规则： ban-wooden-axe

这时候你可以使用以下命令分别列出所有的规则，以及针对 `minecraft:wooden_axe` 这一物品的规则（请保证 `zzzz_ustc` 拥有 `epicbanitem.command.list` 权限）：

```mcfunction
ebi list
ebi list minecraft:wooden_axe
```

以下是可能的输出：

> ----------------------- 规则列表 -----------------------  
> ban-wooden-axe  minecraft:wooden_axe  
> --------------------------- « » ----------------------------  

## 定制规则

我们从上一部分结束，也就是添加规则这一步完成开始。

我们通过以下命令编辑规则（请保证 `zzzz_ustc` 拥有 `epicbanitem.command.edit` 权限）：

```mcfunction
ebi edit ban-wooden-axe
```

以下是可能的输出：

> =============================================  
> 规则: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**ban-wooden-axe**  
> 优先度: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**5**  
> 监听器: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**use  pickup  click  throw  drop  place  break  interact**  
> 世界: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**DIM-1  DIM1  world**  
> 查询规则: &nbsp;**当前设定  默认值  原设定值**  
> 更新规则: &nbsp;**当前设定  默认值  原设定值**  
> **保存**  

上面所有加粗的部分都有鼠标悬浮提示，并都是可以点的。

对于部分情况，点击后可能会弹出一条命令补全。作为示例，我们点击“规则”右侧的 `ban-wooden-axe`，修改它的名字。点击后 EpicBanItem 可能会给出这样一条命令补全：

```mcfunction
ebi cb xxxx ban-wooden-axe
```

我们换个名字，比如说 `ban-worldedit-tool`：

```mcfunction
ebi cb xxxx ban-worldedit-tool
```

然后按下回车（请保证 `zzzz_ustc` 拥有 `epicbanitem.command.callback` 权限）。以下是可能的输出：

> =============================================  
> 规则: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**ban-worldedit-tool**  
> 优先度: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**5**  
> 监听器: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**use  pickup  click  throw  drop  place  break  interact**  
> 世界: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**DIM-1  DIM1  world**  
> 查询规则: &nbsp;**当前设定  默认值  原设定值**  
> 更新规则: &nbsp;**当前设定  默认值  原设定值**  
> **保存**  

然后**点击“保存”**，等待聊天界面弹出一句“已保存”，就说明编辑成功了。

## 小结

EpicBanItem 插件的目标，便是努力做到功能和易用性之间的平衡。上面的教程只是介绍了插件的基本用法，对于更多更详细的用法说明，请移步插件文档剩下的部分。
