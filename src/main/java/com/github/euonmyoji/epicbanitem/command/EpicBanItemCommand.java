package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

import static com.github.euonmyoji.epicbanitem.util.TextUtil.runCommandGui;
import static com.github.euonmyoji.epicbanitem.util.TextUtil.suggestCommandGui;

/**
 * @author 主yinyangshi
 */
public class EpicBanItemCommand {

    public static CommandSpec ebi = CommandSpec.builder()
            .permission("epicbanitem.epicbanitem")
            .executor((src, args) -> {
                src.sendMessage(runCommandGui("/ebi version", "(v)查看插件版本"));
                src.sendMessage(runCommandGui("/ebi reload", "(r)重载插件"));
                src.sendMessage(runCommandGui("/ebi list", "列出手中物品下的所有可用的规则"));
                src.sendMessage(runCommandGui("/ebi list --all", "列出所有可用的规则"));
                src.sendMessage(suggestCommandGui("/ebi list --item", "<item-type> 列出同一个物品下的所有可用的规则", "/ebi list --item <item-type>"));
                return CommandResult.success();
            })
            .build();
}
