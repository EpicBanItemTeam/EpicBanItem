# 用于查找和更新的规则

目前，EpicBanItem 中所有规则都存储于`banitem.conf`中。

规则的作用对象和 MongoDB 类似，但和 MongoDB 使用的 BSON 不同，EpicBanItem 的规则针对的是 NBT 树。

对于 1.12.2 而言，物品的 NBT 树共有三个子标签：

* `id`：代表物品的 id，也就是物品类型，是物品的核心标签
* `Damage`：代表物品的耐久值，又称物品的 meta 值
* `tag`：部分物品存在，代表物品的额外 NBT 标签

NBT 标签中，`Count`标签将被抹除，因此你不能使用 EpicBanItem 修改物品数量。

规则的作用方法同样和 MongoDB 类似。适用于 EpicBanItem 的规则主要分为两大类：

* 用于查找的规则：用于匹配物品 NBT，匹配成功的物品将由插件执行禁用逻辑
* 用于更新的规则：插件执行禁用逻辑时替换物品 NBT 的规则

不存在移除物品的规则。将物品 id 设置为`minecraft:air`即视为移除物品。

## 用于查找的规则

你可以使用`/ebi query`命令在游戏中尝试用于查找的规则，也可以将规则添加到配置文件里。

配置文件中，用于查找的规则的默认值是`{}`。

以下是所有 EpicBanItem 支持的查找运算符。

### 比较运算符

* [`$eq`](https://docs.mongodb.com/manual/reference/operator/query/eq/#op._S_eq)：匹配和特定值相同的值的情况
* [`$gt`](https://docs.mongodb.com/manual/reference/operator/query/gt/#op._S_gt)：匹配比特定值大的数值的情况
* [`$gte`](https://docs.mongodb.com/manual/reference/operator/query/gte/#op._S_gte)：匹配比特定值大或和特定值相等的值的情况
* [`$in`](https://docs.mongodb.com/manual/reference/operator/query/in/#op._S_in)：匹配和特定列表里某个值相同的值的情况
* [`$lt`](https://docs.mongodb.com/manual/reference/operator/query/lt/#op._S_lt)：匹配比特定值小的数值的情况
* [`$lte`](https://docs.mongodb.com/manual/reference/operator/query/lte/#op._S_lte)：匹配比特定值小或和特定值相等的数值的情况
* [`$ne`](https://docs.mongodb.com/manual/reference/operator/query/ne/#op._S_ne)：匹配和特定值不相同的数值的情况
* [`$nin`](https://docs.mongodb.com/manual/reference/operator/query/nin/#op._S_nin)：匹配和特定列表里任何值都不相同的值的情况

### 逻辑运算符

* [`$and`](https://docs.mongodb.com/manual/reference/operator/query/and/#op._S_and)：把若干规则以逻辑与的方式合并到一起，并匹配所有条件都满足的情况
* [`$not`](https://docs.mongodb.com/manual/reference/operator/query/not/#op._S_not)：把特定规则以逻辑非的方式处理，并匹配不满足特定条件的情况
* [`$nor`](https://docs.mongodb.com/manual/reference/operator/query/nor/#op._S_nor)：把若干规则以逻辑或非的方式合并到一起，并匹配不满足任一条件的情况
* [`$or`](https://docs.mongodb.com/manual/reference/operator/query/or/#op._S_or)：把若干规则以逻辑或的方式合并到一起，并匹配满足任一条件的情况

### 元素运算符

* [`$exists`](https://docs.mongodb.com/manual/reference/operator/query/exists/#op._S_exists)：匹配特定位置存在值的规则的情况
* [`$tagType`](#tagtype)：匹配特定位置的值类型和特定 NBT 标签类型一致的规则的情况

### 求值运算符

* [`$regex`](https://docs.mongodb.com/manual/reference/operator/query/regex/#op._S_regex)：匹配满足特定正则表达式的值的情况
* [`$where`](https://docs.mongodb.com/manual/reference/operator/query/where/#op._S_where)：匹配满足特定 JavaScript 表达式结果的情况

### 列表运算符

* [`$all`](https://docs.mongodb.com/manual/reference/operator/query/all/#op._S_all)：匹配数组里所有元素都满足特定规则的情况
* [`$elemMatch`](https://docs.mongodb.com/manual/reference/operator/query/elemMatch/#op._S_elemMatch)：匹配数组里元素满足`$elemMatch`指定的规则的情况
* [`$size`](https://docs.mongodb.com/manual/reference/operator/query/size/#op._S_size)：匹配数组满足特定长度的情况

## 用于更新的规则

你可以使用`/ebi update`命令在游戏中尝试用于更新的规则，也可以将规则添加到配置文件里。

配置文件中，用于更新的规则的默认值是`{"$set": {id: "minecraft:air", Damage: 0}}`。

以下是所有 EpicBanItem 支持的更新运算符。

### 特定位置运算符

* [`$inc`](https://docs.mongodb.com/manual/reference/operator/update/inc/#up._S_inc)：将特定位置的数值增加特定数量
* [`$mul`](https://docs.mongodb.com/manual/reference/operator/update/mul/#up._S_mul)：将特定位置的数值乘以特定数量
* [`$rename`](https://docs.mongodb.com/manual/reference/operator/update/rename/#up._S_rename)：将特定位置的值移动到另一位置
* [`$set`](https://docs.mongodb.com/manual/reference/operator/update/set/#up._S_set)：设置特定位置的值
* [`$unset`](https://docs.mongodb.com/manual/reference/operator/update/unset/#up._S_unset)：将特定位置的值删除

### 特定数组运算符

* [`$`](https://docs.mongodb.com/manual/reference/operator/update/positional/#up._S_)：用于代表规则匹配成立时第一个位置的占位符
* [`$[]`](https://docs.mongodb.com/manual/reference/operator/update/positional-all/#up._S_[])：用于代表规则匹配成立时列表中所有位置的占位符
* [`$pop`](https://docs.mongodb.com/manual/reference/operator/update/pop/#up._S_pop)：移除特定位置列表的第一个或最后一个元素
* [`$pull`](https://docs.mongodb.com/manual/reference/operator/update/pull/#up._S_pull)：移除特定位置列表中匹配某个特定规则的所有元素
* [`$pullAll`](https://docs.mongodb.com/manual/reference/operator/update/pullAll/#up._S_pullAll)：移除特定位置列表中所有匹配的元素

## 自定义运算符

MongoDB 提供的运算符往往不能完全满足需求，因此 EpicBanItem 提供了新的运算符。

### `$tagType`

MongoDB 提供了`$type`运算符用于匹配值类型。但 EpicBanItem 由于的匹配对象是 NBT 树而非 BSON 类型，因此提供了`$tagType`运算符用于匹配 NBT 标签类型。格式如下：

```javascript
{field: {"$tagType": <NBT Tag Type>}}
```

其中，可用的 NBT 标签类型（`<NBT Tag Type>`）有：

* `1`，`byte`
* `2`，`short`
* `3`，`int`
* `4`，`long`
* `5`，`float`
* `6`，`double`
* `7`，`bytearray`，`byte_array`
* `8`，`string`
* `9`，`list`
* `10`，`compound`
* `11`，`intarray`，`int_array`
* `12`，`longarray`，`long_array`

NBT 标签类型不区分大小写。
