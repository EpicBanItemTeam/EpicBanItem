package team.ebi.epicbanitem.command.arg;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.check.CheckRuleService;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

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
        String uncheckedArgString = args.next();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        Optional<CheckRule> optionalCheckRule = Optional.of(uncheckedArgString)
                .filter(argString -> CheckRuleLocation.NAME_PATTERN.matcher(argString).matches())
                .flatMap(argString -> service.getCheckRuleByName(CheckRuleLocation.of(argString)));
        return optionalCheckRule.orElseThrow(() -> args.createError(
                EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.args.checkRule.notFound",
                        Tuple.of("name", uncheckedArgString))
        ));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix = args.nextIfPresent().orElse("");
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        return service.getNames().stream()
                .map(CheckRuleLocation::toString)
                .filter(new StartsWithPredicate(prefix))
                .collect(ImmutableList.toImmutableList());
    }
}
