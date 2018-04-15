package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

import static com.github.euonmyoji.epicbanitem.util.TextUtil.runCommandGuiThenSend;
import static com.github.euonmyoji.epicbanitem.util.TextUtil.suggestCommandGuiThenSend;

/**
 * @author 主yinyangshi
 */
public class EpicBanItemCommand {

    public static CommandSpec ebi = CommandSpec.builder()
            .permission("epicbanitem.epicbanitem")
            .executor((src, args) -> {
                runCommandGuiThenSend(src, "/ebi version", "(v)查看插件版本");
                runCommandGuiThenSend(src, "/ebi reload", "(r)重载插件");
                runCommandGuiThenSend(src, "/ebi list", "列出手中物品下的所有可用的规则");
                runCommandGuiThenSend(src, "/ebi list --all", "列出所有可用的规则");
                suggestCommandGuiThenSend(src, "/ebi list --item", "列出同一个物品下的所有可用的规则", "<item-type>");
                suggestCommandGuiThenSend(src, "/ebi query", "使用特定规则检查手中物品，并列出nbt等信息", "<query-rule>");
                suggestCommandGuiThenSend(src, "/ebi show", "展示手中物品的特定的规则", "<rule-name>");
                suggestCommandGuiThenSend(src, "/ebi show --item", "展示某个特定的规则", "<item-type> <rule-name>");
                suggestCommandGuiThenSend(src, "/ebi create", "添加手中物品的某个规则", "<rule-name>");
                suggestCommandGuiThenSend(src, "/ebi update", "设置手中物品规则的某些值", "<rule-name>");
                suggestCommandGuiThenSend(src, "/ebi update --item", "设置某个规则的某些值", "<item-type> <rule-name>");
                runCommandGuiThenSend(src, "/ebi apply", "将所有可用的规则全部应用到手中的物品上");
                suggestCommandGuiThenSend(src, "/ebi apply", "将某规则应用到手中的物品上", "<rule-name>");
                return CommandResult.success();
            })
            .child(Apply.apply, "apply", "a")
            .child(Create.create, "create", "c")
            .child(List.list, "list", "l")
            .child(Query.query, "query", "q")
            .child(Show.show, "show", "s")
            .child(Update.update, "update", "u")
            .child(Plugin.reload, "reload", "r")
            .build();
}
