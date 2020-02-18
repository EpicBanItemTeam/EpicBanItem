package com.github.euonmyoji.epicbanitem.command.arg;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

@NonnullByDefault
public class ArgPatternString extends CommandElement {
    private Pattern pattern;
    private String errorMessageKey = "epicbanitem.args.pattern.notMatch";

    ArgPatternString(@Nullable Text key, Pattern pattern) {
        this(key, pattern, null);
    }

    ArgPatternString(@Nullable Text key, Pattern pattern, @Nullable String errorMessageKey) {
        super(key);
        this.pattern = Objects.requireNonNull(pattern);
        if (errorMessageKey != null) {
            this.errorMessageKey = errorMessageKey;
        }
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String string = args.next();
        if (pattern.matcher(string).matches()) {
            return string;
        } else {
            throw args.createError(EpicBanItem.getLocaleService().getTextWithFallback(errorMessageKey, Tuple.of("pattern", pattern)));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }
}
