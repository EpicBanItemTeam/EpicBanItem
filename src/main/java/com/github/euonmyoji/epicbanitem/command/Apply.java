package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

public class Apply {

    static CommandSpec apply = CommandSpec.builder()
            .permission("epicbanitem.apply")
            .executor((src, args) -> {
                //something
                return CommandResult.success();
            })
            .build();
}
