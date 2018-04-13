package com.github.euonmyoji.epicbanitem.commands;

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
                src.sendMessage(runCommandGui("/ebi version", "(v)查看插件版本", null));
                src.sendMessage(runCommandGui("/ebi reload", "(r)重载插件", null));
                return CommandResult.success();
            })
            .build();
}
