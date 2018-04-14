package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

class List {

    static CommandSpec list = CommandSpec.builder()
            .permission("epicbanitem.list")
            .executor((src, args) -> {
                //something
                return CommandResult.success();
            })
            .build();
}
