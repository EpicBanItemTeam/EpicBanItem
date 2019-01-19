package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckResult;
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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class CommandCheck extends AbstractCommand {

    private CheckRuleService service;

    CommandCheck() {
        super("check", "k");
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
        Map<String, CheckRule> checkRules = new TreeMap<>();
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        if (lookAt) {
            Optional<BlockSnapshot> optional = CommandCreate.getBlockLookAt(src);
            BlockSnapshot blockSnapshot = optional.orElseThrow(() -> new CommandException(getMessage("noBlock")));
            World world = ((Locatable) src).getWorld();
            for (String trigger : Triggers.getDefaultTriggers()) {
                CheckResult checkResult = service.check(blockSnapshot, world, trigger, null);
                checkResult.getBreakRules().forEach(checkRule -> checkRules.put(checkRule.getName(), checkRule));
            }
        } else {
            Optional<Tuple<HandType, ItemStack>> optional = CommandCreate.getItemInHand(src);
            ItemStack itemStack = optional.orElseThrow(() -> new CommandException(getMessage("noItem"))).getSecond();
            World world = ((Locatable) src).getWorld();
            for (String trigger : Triggers.getDefaultTriggers()) {
                CheckResult checkResult = service.check(itemStack, world, trigger, null);
                checkResult.getBreakRules().forEach(checkRule -> checkRules.put(checkRule.getName(), checkRule));
            }
        }
        Text.Builder info = Text.builder();
        for (CheckRule rule : checkRules.values()) {
            info.append(rule.toText(), Text.NEW_LINE);
        }
        src.sendMessage(info.toText());
        return CommandResult.success();
    }
}
