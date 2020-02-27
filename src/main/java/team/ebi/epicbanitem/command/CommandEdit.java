package team.ebi.epicbanitem.command;

import com.google.inject.Singleton;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.command.arg.EpicBanItemArgs;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public class CommandEdit extends AbstractCommand {

    public CommandEdit() {
        super("edit");
    }

    @Override
    public CommandElement getArgument() {
        return EpicBanItemArgs.checkRule(Text.of("rule"));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(getMessage("playerOnly"));
        }
        //noinspection OptionalGetWithoutIsPresent
        CheckRule rule = args.<CheckRule>getOne("rule").get();
        CommandEditor.add((Player) src, rule, true);
        return CommandResult.success();
    }
}
