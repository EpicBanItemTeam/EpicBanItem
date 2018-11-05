package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
class CommandShow extends AbstractCommand {
    public CommandShow() {
        super("show", "s");
    }

    @Override
    public CommandElement getArgument() {
        return EpicBanItemArgs.checkRule(Text.of("check-rule"));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        // noinspection ConstantConditions
        src.sendMessage(args.<CheckRule>getOne("check-rule").get().info());
        return CommandResult.success();
    }
}
