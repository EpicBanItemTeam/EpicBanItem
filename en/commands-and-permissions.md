# Commands and Permissions

## Commands

The formal name of the command related to EpicBanItem is named `/ebi`. There are also other aliases available, such as `/epicbanitem`, `/banitem`, and `/bi`. All the introductions will use `/ebi` as command prefix by default.

There is not any command for `reload`ing configurations in EpicBanItem. THe reason is that all the change events of config files will be listened, in order to reload files when the events are fired.

### `/ebi help [<sub-command>]`

Display brief introduction of all the subcommands if the `subcommand` is not specified, otherwise display relatively detailed introduction for a particular subcommand.

### `/ebi query [-l] [<query-rule...>]`

> TBD

### `/ebi update [-l] <update-rule...>`

> TBD

### `/ebi list [<item-type>]`

> TBD

### `/ebi check [-l]`

> TBD

### `/ebi show <item-type> <check-rule>`

> TBD

### `/ebi create <rule-name> [--no-capture] [<query-rule…>]`

> TBD

### `/ebi remove <rule-name>`

> TBD

## Permissions

All the permission strings start with `epicbanitem`.

| Name of the Permission           | Description of the Permission                                     |
| :------------------------------- | :---------------------------------------------------------------- |
| `epicbanitem`                    | All the permissions related to EpicBanItem                        |
| `epicbanitem.command`            | All the permissions for using commands related to EpicBanItem     |
| `epicbanitem.command.query`      | Permission for using `/ebi query` command                         |
| `epicbanitem.command.update`     | Permission for using `/ebi update` command                        |
| `epicbanitem.command.list`       | Permission for using `/ebi list` command                          |
| `epicbanitem.command.check`      | Permission for using `/ebi check` command                         |
| `epicbanitem.command.show`       | Permission for using `/ebi show` command                          |
| `epicbanitem.command.create`     | Permission for using `/ebi create` command                        |
| `epicbanitem.command.remove`     | Permission for using `/ebi remove` command                        |
| `epicbanitem.bypass`             | Permission that bypass all the rules which players participate in |
| `epicbanitem.bypass.<rule-name>` | Permission that bypass the rule named `<rule-name>`               |
