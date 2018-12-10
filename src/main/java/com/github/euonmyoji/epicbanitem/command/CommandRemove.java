package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
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
public class CommandRemove extends AbstractCommand {
    public CommandRemove() {
        super("remove", "delete", "del");
    }

    @Override
    public CommandElement getArgument() {
        return EpicBanItemArgs.checkRule(Text.of("rule"));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        // noinspection ConstantConditions
        CheckRule checkRule = args.<CheckRule>getOne("rule").get();
        service.removeRule(checkRule).thenAccept(succeed -> {
            if (succeed) {
                src.sendMessage(getMessage("succeed", "rule", checkRule.getName()));
            }
        });
        return CommandResult.success();
    }
}
