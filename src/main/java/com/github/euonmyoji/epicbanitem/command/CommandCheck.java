package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author EBI
 */
public class CommandCheck extends AbstractCommand {

    private CheckRuleService service;

    public CommandCheck() {
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
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("Player only."));
        }
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        boolean lookAt = args.hasAny("l");
        List<CheckRule> breakRules = new ArrayList<>();
        // TODO: lookat
        ItemStack itemStack = ((Player) src).getItemInHand(HandTypes.MAIN_HAND).orElseThrow(() -> new CommandException(Text.of("Nothing in hand.")));
        for (CheckRule rule : service.getCheckRules(itemStack.getType())) {
            CheckResult result = CheckResult.empty();
            rule.check(itemStack, result, getEnabledWorld(rule), getEnabledTrigger(rule), null);
            breakRules.addAll(result.getBreakRules());
        }
        Text.Builder info = Text.builder();
        for (CheckRule rule : breakRules) {
            info.append(rule.toText(), Text.NEW_LINE);
        }
        src.sendMessage(info.toText());
        return CommandResult.success();
    }

    private World getEnabledWorld(CheckRule rule)  throws CommandException {
        for (World world : Sponge.getServer().getWorlds()) {
            if (rule.isEnabledWorld(world)) {
                return world;
            }
        }
        throw new CommandException(getMessage("noFitWorld"));
    }

    private String getEnabledTrigger(CheckRule rule)  throws CommandException {
        for(String trigger:Triggers.getDefaultTriggers()){
            if(rule.isEnabledTrigger(trigger)){
                return trigger;
            }
        }
        throw new CommandException(getMessage("noTrigger"));
    }

}
