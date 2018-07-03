package com.github.euonmyoji.epicbanitem.command.arg;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NonnullByDefault
class ArgCheckRule extends CommandElement {

    ArgCheckRule(@Nullable Text key) {
        super(key);
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        //noinspection ConstantConditions  不会传empty的 除非有什么改变了宇宙
        ItemType itemType = context.<ItemType>getOne("item-type").get();
        String argString = args.next();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        CheckRule rule = null;
        Optional<CheckRule> optionalCheckRule = service.getCheckRule(itemType, argString);
        if (optionalCheckRule.isPresent()) {
            rule = optionalCheckRule.get();
        } else {
            for (CheckRule rule1 : service.getCheckRules(itemType)) {
                if (rule1.getName().equalsIgnoreCase(argString)) {
                    rule = rule1;
                    break;
                }
            }
        }
        if (rule != null) {
            context.putArg(getKey(), rule);
        } else {
            throw args.createError(Text.of("rule为null"));
        }
    }

    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        //noinspection ConstantConditions  不会传empty的 除非有什么改变了宇宙
        ItemType itemType = context.<ItemType>getOne("item-type").get();
        String prefix = args.nextIfPresent().orElse("").toLowerCase();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        return service.getCheckRules(itemType).stream().map(CheckRule::getName).filter(s -> s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
    }
}