package com.github.euonmyoji.epicbanitem.command.arg;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;

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
            throw args.createError(EpicBanItem.getMessages().getMessage("epicbanitem.args.range.outOfRange", "input", integer, "min", min, "max", max));
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
