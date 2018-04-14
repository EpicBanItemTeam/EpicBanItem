package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

class Create {

    static CommandSpec create = CommandSpec.builder()
            .permission("epicbanitem.create")
            .executor((src, args) -> {
                //something
                return CommandResult.success();
            })
            .build();
}
