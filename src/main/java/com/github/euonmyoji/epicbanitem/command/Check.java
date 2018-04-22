package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class Check implements CommandExecutor {
    public static CommandSpec check = CommandSpec.builder()
            //todo:<hand|lookat>
            .permission("epicbanitem.check")
            .arguments(
                    GenericArguments.string(Text.of("trigger"))
            )
            .executor(new Check())
            .build();

    private CheckRuleService service;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(service == null){
            service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        }
        if(!(src instanceof Player)){
            throw new CommandException(Text.of("Player only."));
        }
        String trigger = args.<String>getOne("trigger").get();
        //todo:lookat
        ItemStack itemStack = ((Player) src).getItemInHand(HandTypes.MAIN_HAND).orElseThrow(()->new CommandException(Text.of("Nothing in hand.")));
        CheckResult result = service.check(itemStack,((Player) src).getWorld(),trigger,null);
        src.sendMessage(result.getText());
        return CommandResult.success();
    }

}
