package team.ebi.epicbanitem.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.command.arg.EpicBanItemArgs;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public class CommandRemove extends AbstractCommand {
    @Inject
    private CheckRuleService service;

    CommandRemove() {
        super("remove", "delete", "del");
    }

    @Override
    public CommandElement getArgument() {
        return EpicBanItemArgs.checkRule(Text.of("rule"));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        CheckRule checkRule = args.<CheckRule>getOne("rule").orElseThrow(NoSuchFieldError::new);
        service
            .removeRule(checkRule)
            .thenAccept(
                succeed -> {
                    if (succeed) {
                        src.sendMessage(getMessage("succeed", Tuple.of("rule", checkRule.getName())));
                    }
                }
            );
        return CommandResult.success();
    }
}
