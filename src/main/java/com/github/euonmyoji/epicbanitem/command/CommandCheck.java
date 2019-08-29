package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.api.CheckRuleTrigger;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class CommandCheck extends AbstractCommand {

    private static Map<String, CheckRule> checkRuleContext = new LinkedHashMap<>();

    CommandCheck() {
        super("check", "k");
    }

    public static void addContext(CheckRule rule) {
        checkRuleContext.put(rule.getName(), rule);
    }

    @Override
    public CommandElement getArgument() {
        // TODO: 可选的世界?
        // TODO: 可选的Trigger?
        return GenericArguments.flags()
                .flag("l")
                .buildWith(
                        GenericArguments.none()
                );
    }

    @Override
    @Nonnull
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        boolean lookAt = args.hasAny("l");
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        if (lookAt) {
            checkRuleContext.clear();
            Optional<BlockSnapshot> optional = CommandCreate.getBlockLookAt(src);
            BlockSnapshot blockSnapshot = optional.orElseThrow(() -> new CommandException(getMessage("noBlock")));
            World world = ((Locatable) src).getWorld();
            for (CheckRuleTrigger trigger : Triggers.getTriggers().values()) {
                service.check(blockSnapshot, world, trigger, null);
            }
        } else {
            checkRuleContext.clear();
            Optional<Tuple<HandType, ItemStack>> optional = CommandCreate.getItemInHand(src);
            ItemStack itemStack = optional.orElseThrow(() -> new CommandException(getMessage("noItem"))).getSecond();
            World world = ((Locatable) src).getWorld();
            for (CheckRuleTrigger trigger : Triggers.getTriggers().values()) {
                service.check(itemStack, world, trigger, null);
            }
        }
        Text.Builder info = Text.builder();
        for (Iterator<CheckRule> it = checkRuleContext.values().iterator(); it.hasNext(); it.remove()) {
            info.append(it.next().toText(), Text.NEW_LINE);
        }
        src.sendMessage(info.toText());
        return CommandResult.success();
    }
}
