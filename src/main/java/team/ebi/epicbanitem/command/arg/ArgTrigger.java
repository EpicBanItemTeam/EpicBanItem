package team.ebi.epicbanitem.command.arg;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.check.Triggers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class ArgTrigger extends CommandElement {

    ArgTrigger(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String arg = args.next().toLowerCase();
        if (Triggers.getTriggers().containsKey(arg)) {
            return arg;
        }
        throw args.createError(EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.args.trigger.notFound", Tuple.of("name", arg)));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix = args.nextIfPresent().orElse("").toLowerCase();
        return Triggers.getTriggers().keySet().stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
    }
}
