package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

class Update {
    static CommandSpec update = CommandSpec.builder()
            .permission("epicbanitem.update")
            .executor((src, args) -> {
                //something
                return CommandResult.success();
            })
            .build();
}
