package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

class Show {
    static CommandSpec show = CommandSpec.builder()
            .permission("epicbanitem.show")
            .executor((src, args) -> {
                //something
                return CommandResult.success();
            })
            .build();
}
