# Rules for Querying and Updating

All the rules of EpicBanItem are stored in `banitem.conf` currently.

The rule target is similar to MongoDB, but instead of BSON in MongoDB, NBT tree is targeted in EpicBanItem.

There are three sub-tags in the NBT tree of an item in 1.12.2:

* `id` for the id of an item, which is the core tag of an item
* `Damage` for the damage value of an item, which is also called meta
* `tag` for the extra NBT tag of an item, which exists in some items only

The tag named `Count` will be erased in all of the NBT tags, so it is not possible to modify the stack count of items in EpicBanItem.

The way for rules to take effect is also similar to MongoDB. It can be divided into two categories in EpicBanItem:

* Rules for querying: used to match the NBT of an item, and the plugin will start the ban procedure for every item having been successfully matched
* Rules for updating: used to modify the NBT of an item when the ban procedure started by the plugin is being executed

There are not any rules dedicated to removing items. Setting the id to `minecraft:air` is considered as removing the item.

## Rules for Querying

Rules for querying can be tried by using command `/ebi query` in game, and can also be stored in config files.

The default rule for querying in config files is `{}`.

All the query operators supported by EpicBanItems are shown below.

### Comparison Operator

* [`$eq`](https://docs.mongodb.com/manual/reference/operator/query/eq/#op._S_eq): Matches values that are equal to a specified value
* [`$gt`](https://docs.mongodb.com/manual/reference/operator/query/gt/#op._S_gt): Matches values that are greater than a specified value
* [`$gte`](https://docs.mongodb.com/manual/reference/operator/query/gte/#op._S_gte): Matches values that are greater than or equal to a specified value
* [`$in`](https://docs.mongodb.com/manual/reference/operator/query/in/#op._S_in): Matches any of the values specified in an array
* [`$lt`](https://docs.mongodb.com/manual/reference/operator/query/lt/#op._S_lt): Matches values that are less than a specified value
* [`$lte`](https://docs.mongodb.com/manual/reference/operator/query/lte/#op._S_lte): Matches values that are less than or equal to a specified value
* [`$ne`](https://docs.mongodb.com/manual/reference/operator/query/ne/#op._S_ne): Matches all values that are not equal to a specified value
* [`$nin`](https://docs.mongodb.com/manual/reference/operator/query/nin/#op._S_nin): Matches none of the values specified in an array

### Logical Operator

* [`$and`](https://docs.mongodb.com/manual/reference/operator/query/and/#op._S_and): Joins query rules with a logical AND and then matches all the rules
* [`$not`](https://docs.mongodb.com/manual/reference/operator/query/not/#op._S_not): Inverts the query rule and then matches what fails to match the rule
* [`$nor`](https://docs.mongodb.com/manual/reference/operator/query/nor/#op._S_nor): Joins query rules with a logical NOR and then matches what fails to match all the rules
* [`$or`](https://docs.mongodb.com/manual/reference/operator/query/or/#op._S_or): Joins query rules with a logical OR and then matches what matches any of the rules

### Element Operator

* [`$exists`](https://docs.mongodb.com/manual/reference/operator/query/exists/#op._S_exists): Matches what have the specified field
* [`$tagType`](#tagtype): Matches if the value type of a field is of the specified NBT type

### Evaluation Operator

* [`$regex`](https://docs.mongodb.com/manual/reference/operator/query/regex/#op._S_regex): Matches values that match a specified regular expression
* [`$where`](https://docs.mongodb.com/manual/reference/operator/query/where/#op._S_where): Matches what satisfies a JavaScript expression

### Array Operator

* [`$all`](https://docs.mongodb.com/manual/reference/operator/query/all/#op._S_all): Matches arrays that contain all elements specified in the query
* [`$elemMatch`](https://docs.mongodb.com/manual/reference/operator/query/elemMatch/#op._S_elemMatch): Matches arrays if element in the array field matches all the specified `$elemMatch` rules
* [`$size`](https://docs.mongodb.com/manual/reference/operator/query/size/#op._S_size): Matches arrays if the array field is a specified size

## Rules for Updating

Rules for updating can be tried by using command `/ebi update` in game, and can also be stored in config files.

The default rule for updating in config files is `{"$set": {id: "minecraft:air", Damage: 0}}`.

All the update operators supported by EpicBanItems are shown below.

### Operator for Fields

* [`$inc`](https://docs.mongodb.com/manual/reference/operator/update/inc/#up._S_inc): Increments the value of the field by the specified amount
* [`$mul`](https://docs.mongodb.com/manual/reference/operator/update/mul/#up._S_mul): Multiplies the value of the field by the specified amount
* [`$rename`](https://docs.mongodb.com/manual/reference/operator/update/rename/#up._S_rename): Renames a field
* [`$set`](https://docs.mongodb.com/manual/reference/operator/update/set/#up._S_set): Sets the value of a field
* [`$unset`](https://docs.mongodb.com/manual/reference/operator/update/unset/#up._S_unset): Removes the specified field

### Operator for Arrays

* [`$`](https://docs.mongodb.com/manual/reference/operator/update/positional/#up._S_): Acts as a placeholder to update the first element that matches the query rule
* [`$[]`](https://docs.mongodb.com/manual/reference/operator/update/positional-all/#up._S_[]): Acts as a placeholder to update all elements in an array for what matches the query rule
* [`$pop`](https://docs.mongodb.com/manual/reference/operator/update/pop/#up._S_pop): Removes the first or last item of an array
* [`$pull`](https://docs.mongodb.com/manual/reference/operator/update/pull/#up._S_pull): Removes all array elements that match a specified query
* [`$pullAll`](https://docs.mongodb.com/manual/reference/operator/update/pullAll/#up._S_pullAll): Removes all matching values from an array

## Custom Operators

The operators provided by MongoDB are not always enough, so some new operators are provided in EpicBanItem.

### `$tagType`

There has already been an operator named `$type` provided by MongoDB. However, instead of BSON types, NBT tree is targeted in EpicBanItem, so an operator named `$tagType` is provided for matching NBT tag types. Here is the syntax format:

```javascript
{field: {"$tagType": <NBT Tag Type>}}
```

`<NBT Tag Types>` can be specified as one of the following values:

* `1`, `byte`
* `2`, `short`
* `3`, `int`
* `4`, `long`
* `5`, `float`
* `6`, `double`
* `7`, `bytearray`, `byte_array`
* `8`, `string`
* `9`, `list`
* `10`, `compound`
* `11`, `intarray`, `int_array`
* `12`, `longarray`, `long_array`

NBT tag types are case insensitive.
