package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

public class Plugin {

    static CommandSpec reload = CommandSpec.builder()
            .permission("epicbanitem.reload")
            .executor((src, args) -> {
                EpicBanItem.reload();
                return CommandResult.success();
            })
            .build();
}
