# 命令和权限

## 命令

EpicBanItem的相关命令的正式名称为`/ebi`。插件同时注册了`/epicbanitem`、`/banitem`、`/bi`等三个别名。以下所有介绍，如有用到命令，均以`/ebi`开头。

EpicBanItem不存在`reload`的相关命令。这是因为插件会自动监听配置文件，并在发生修改后自动重新加载。

### `/ebi help [<sub-command>]`

如`subcommand`不存在，显示所有命令的简要帮助，如存在，显示对应子命令的详细帮助。

### `/ebi query [-l] [<query-rule...>]`

根据`query-rule`提供的匹配规则，匹配手上物品，或者目视方块对应物品。

若`query-rule`为空，则以上一次输入的`query-rule`为匹配规则。

若`-l`存在，则匹配目视方块，否则匹配手上物品。

`query`可以`q`作为简写。

### `/ebi update [-l] <update-rule...>`

根据`update-rule`提供的更新规则，匹配手上物品，或者目视方块对应物品。

若`-l`存在，则更新目视方块，否则匹配手上物品。

`update`可以`u`作为简写。

### `/ebi list [<item-type>]`

列出所有能够匹配到`item-type`对应物品的规则。

如果`item-type`为空，则列出所有规则。

`list`可以`l`作为简写。

### `/ebi check [-l]`

检查并列出满足手上物品，或者方块对应物品的所有规则。

若`-l`存在，则检查目视方块，否则检查手上物品。

`check`可以`k`作为简写。

### `/ebi show <item-type> <check-rule>`

显示对应`item-type`和`check-rule`的特定规则的详细信息。

`show`可以`s`作为简写。

### `/ebi create <rule-name> [--no-capture] [<query-rule…>]`

添加名为`rule-name`的规则，其中匹配规则为`query-rule`（若未指定，则取默认值`{}`）。

`query-rule`通常会和手上物品的类型合并，如不欲合并，请加`--no-capture`。

`create`可以`c`作为简写。

### `/ebi remove <rule-name>`

移除名为`rule-name`的规则。

`remove`可以`delete`和`del`作为别名。

### `/ebi edit <rule-name>`

为`rule-name`这一规则打开一个编辑器。

## 权限

所有权限均以 `epicbanitem` 开头。

| 权限名称                     | 权限描述                             |
| :------------------------------- | :--------------------------------------- |
| `epicbanitem`                    | 所有和EpicBanItem有关的权限      |
| `epicbanitem.command`            | 使用所有EpicBanItem命令的权限   |
| `epicbanitem.command.query`      | 使用`/ebi query`命令的权限        |
| `epicbanitem.command.update`     | 使用`/ebi update`命令的权限       |
| `epicbanitem.command.list`       | 使用`/ebi list`命令的权限         |
| `epicbanitem.command.check`      | 使用`/ebi check`命令的权限        |
| `epicbanitem.command.show`       | 使用`/ebi show`命令的权限         |
| `epicbanitem.command.create`     | 使用`/ebi create`命令的权限       |
| `epicbanitem.command.remove`     | 使用`/ebi remove`命令的权限       |
| `epicbanitem.command.edit`       | 使用`/ebi edit`命令的权限         |
| `epicbanitem.bypass`             | 无视所有针对玩家规则的权限  |
| `epicbanitem.bypass.<rule-name>` | 无视`<rule-name>`这一规则的权限 |

在检查无视规则的权限时，插件会在默认权限上下文后追加一条形如`epicbanitem-trigger=<trigger>`的新的上下文。比如说如果服务器希望所有属于`default`组的玩家都无视名为`example`的规则，但仅限于`pickup`触发器，那么安装有 [LuckPerms](https://ore.spongepowered.org/Luck/LuckPerms) 的服务器可以试试下面的命令：

```mcfunction
lp group default permission set epicbanitem.bypass.example epicbanitem-trigger=pickup
```
