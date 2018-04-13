package com.github.euonmyoji.epicbanitem.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

import static org.spongepowered.api.text.Text.of;

/**
 * @author 主yinyangshi
 */
public class EpicBanItemCommand {

    public static CommandSpec ebi = CommandSpec.builder()
            .permission("epicbanitem.epicbanitem")
            .executor((src, args) -> {
                src.sendMessage(of());
                return CommandResult.success();
            })
            .build();
}
