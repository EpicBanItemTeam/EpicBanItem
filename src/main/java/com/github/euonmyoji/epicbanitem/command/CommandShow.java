package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
class CommandShow extends AbstractCommand {
    CommandShow() {
        super("show", "s");
    }

    @Override
    public CommandElement getArgument() {
        return EpicBanItemArgs.checkRule(Text.of("check-rule"));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        src.sendMessage(args.<CheckRule>getOne("check-rule").orElseThrow(NoSuchFieldError::new).info());
        return CommandResult.success();
    }
}
