package team.ebi.epicbanitem.command;

import com.google.inject.Singleton;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.command.arg.EpicBanItemArgs;

/**
 * @author The EpicBanItem Team
 */
@Singleton
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
        src.sendMessage(args.<CheckRule>getOne("check-rule").orElseThrow(NoSuchFieldError::new).toText());
        return CommandResult.success();
    }
}
