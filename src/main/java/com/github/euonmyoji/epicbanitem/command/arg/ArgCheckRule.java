package com.github.euonmyoji.epicbanitem.command.arg;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author EBI
 */
@NonnullByDefault
class ArgCheckRule extends CommandElement {

    ArgCheckRule(@Nullable Text key) {
        super(key);
    }

    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String argString = args.next();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        Optional<CheckRule> optionalCheckRule = service.getCheckRule(argString);
        if (optionalCheckRule.isPresent()) {
            return optionalCheckRule.get();
        } else {
            throw args.createError(EpicBanItem.getMessages()
                    .getMessage("epicbanitem.args.checkRule.notFound", "name", argString));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix = args.nextIfPresent().orElse("").toLowerCase();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        return service.getRuleNames().stream().filter(s -> s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
    }
}