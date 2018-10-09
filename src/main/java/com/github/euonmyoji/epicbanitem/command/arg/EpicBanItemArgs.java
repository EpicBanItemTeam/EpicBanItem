package com.github.euonmyoji.epicbanitem.command.arg;

import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@SuppressWarnings("WeakerAccess")
public class EpicBanItemArgs {

    public static CommandElement itemOrHand(Text key, boolean explicitHand) {
        return new ArgItemOrHand(key, explicitHand);
    }

    public static CommandElement checkRule(Text key) {
        return new ArgCheckRule(key);
    }

    public static CommandElement checkRuleWithItem(Text key, boolean explicitHand) {
        return GenericArguments.seq(
                itemOrHand(Text.of("item-type"), explicitHand),
                new ArgItemCheckRule(key)
        );
    }
}
