package com.github.euonmyoji.epicbanitem.command.arg;

import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class EpicBanItemArgs {

    private static CommandElement itemOrHand(Text key, boolean explicitHand) {
        return new ArgItemOrHand(key, explicitHand);
    }

    public static CommandElement checkRule(Text key) {
        return checkRule(key, false);
    }

    public static CommandElement checkRule(Text key, boolean explicitHand) {
        return GenericArguments.seq(
                itemOrHand(Text.of("item-type"), explicitHand),
                new ArgCheckRule(key)
        );
    }
}
