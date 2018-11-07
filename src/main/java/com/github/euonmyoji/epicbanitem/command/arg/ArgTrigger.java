package com.github.euonmyoji.epicbanitem.command.arg;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@NonnullByDefault
public class ArgTrigger extends CommandElement {

    ArgTrigger(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String arg = args.next().toLowerCase();
        if (Triggers.getDefaultTriggers().contains(arg)) {
            return arg;
        }
        throw args.createError(EpicBanItem.getMessages()
                .getMessage("epicbanitem.args.trigger.notFound", "name", arg));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix = args.nextIfPresent().orElse("").toLowerCase();
        return Triggers.getDefaultTriggers().stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
    }
}
