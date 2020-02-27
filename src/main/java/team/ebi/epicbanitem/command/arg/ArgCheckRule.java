package team.ebi.epicbanitem.command.arg;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.check.CheckRuleService;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author The EpicBanItem Team
 */
@NonnullByDefault
class ArgCheckRule extends CommandElement {

    ArgCheckRule(@Nullable Text key) {
        super(key);
    }

    @Override
    protected CheckRule parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String argString = args.next();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        Optional<CheckRule> optionalCheckRule = service.getCheckRuleByName(argString);
        if (optionalCheckRule.isPresent()) {
            return optionalCheckRule.get();
        } else {
            throw args.createError(EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.args.checkRule.notFound", Tuple.of("name", argString)));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix = args.nextIfPresent().orElse("").toLowerCase();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        return service.getNames().stream().filter(s -> s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
    }
}
