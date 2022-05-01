package team.ebi.epicbanitem.command.arg;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author The EpicBanItem Team
 */
@NonnullByDefault
public class ArgRangeInteger extends CommandElement {

    public static CommandElement range(Text key, int min, int max) {
        return new ArgRangeInteger(GenericArguments.integer(key), min, max);
    }

    private CommandElement warp;
    private int min;
    private int max;

    private ArgRangeInteger(CommandElement warp, int min, int max) {
        super(warp.getKey());
        this.warp = warp;
        this.min = min;
        this.max = max;
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        warp.parse(source, args, context);
        Integer integer = context.<Integer>getOne(warp.getKey()).orElseThrow(IllegalStateException::new);
        if (integer < min || integer > max) {
            throw args.createError(
                EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.args.range.outOfRange", Tuple.of("input", integer), Tuple.of("min", min), Tuple.of("max", max))
            );
        }
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) {
        return null;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return warp.complete(src, args, context);
    }
}
