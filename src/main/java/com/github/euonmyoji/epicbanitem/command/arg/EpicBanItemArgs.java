package com.github.euonmyoji.epicbanitem.command.arg;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
@SuppressWarnings("WeakerAccess")
public class EpicBanItemArgs {

    private static final Map<String, Tristate> TRISTATE_MAP = ImmutableMap.<String, Tristate>builder()
            .put("false", Tristate.FALSE)
            .put("undefined", Tristate.UNDEFINED)
            .put("true", Tristate.TRUE)
            .build();


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

    public static CommandElement tristate(Text key) {
        return GenericArguments.choices(key, TRISTATE_MAP);
    }

    public static CommandElement trigger(Text key) {
        return new ArgTrigger(key);
    }
}
