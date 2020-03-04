package team.ebi.epicbanitem.command.arg;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.CheckRuleIndex;
import team.ebi.epicbanitem.api.CheckRuleLocation;
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
class ArgItemCheckRule extends CommandElement {

    ArgItemCheckRule(@Nullable Text key) {
        super(key);
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        ItemType itemType = context.<ItemType>getOne("item-type").orElseThrow(NoSuchFieldError::new);
        CheckRuleIndex index = CheckRuleIndex.of(itemType);
        String uncheckedArgString = args.next();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        Optional<CheckRule> optionalCheckRule = Optional.of(uncheckedArgString)
                .filter(argString -> CheckRuleLocation.NAME_PATTERN.matcher(argString).matches())
                .flatMap(argString -> service.getCheckRuleByNameAndIndex(index, CheckRuleLocation.of(argString)));
        context.putArg(getKey(), optionalCheckRule.orElseThrow(() -> args.createError(
                EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.args.itemCheckRule.notFound",
                        Tuple.of("name", uncheckedArgString), Tuple.of("item", itemType.getId()))
        )));
    }

    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        ItemType itemType = context.<ItemType>getOne("item-type").orElseThrow(NoSuchFieldError::new);
        CheckRuleIndex index = CheckRuleIndex.of(itemType);
        String prefix = args.nextIfPresent().orElse("").toLowerCase();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        return service
            .getCheckRulesByIndex(index)
            .stream()
            .map(CheckRule::getName)
            .map(CheckRuleLocation::toString)
            .filter(new StartsWithPredicate(prefix))
            .collect(Collectors.toList());
    }
}
