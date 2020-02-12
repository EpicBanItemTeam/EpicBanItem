package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.api.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;

public class CommandCheckAll extends AbstractCommand {
    private CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);

    public CommandCheckAll() {
        super("checkall", "ca");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optional(EpicBanItemArgs.checkRule(Text.of("rule")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Optional<CheckRule> rule = args.getOne("rule");
        if (rule.isPresent()) {
            Sponge
                .getServer()
                .getOnlinePlayers()
                .forEach(
                    player ->
                        replaceItems(player, service.checkInventory(player.getInventory(), player.getWorld(), Triggers.JOIN, rule.get(), player))
                );
        } else {
            Sponge
                .getServer()
                .getOnlinePlayers()
                .forEach(player -> replaceItems(player, service.checkInventory(player.getInventory(), player.getWorld(), Triggers.JOIN, player)));
        }

        src.sendMessage(Text.of(TextColors.GREEN, EpicBanItem.getMessages().getMessage("epicbanitem.command.checkall.success")));
        return CommandResult.success();
    }

    @SuppressWarnings({ "DuplicatedCode", "OptionalGetWithoutIsPresent" })
    private void replaceItems(Player player, Set<Tuple<CheckResult, Inventory>> tuples) {
        tuples
            .stream()
            .filter(tuple -> tuple.getFirst().isBanned())
            .forEach(
                tuple ->
                    tuple
                        .getFirst()
                        .getFinalView()
                        .map(dataContainer -> NbtTagDataUtil.toItemStack(dataContainer, tuple.getSecond().peek().get().getQuantity()))
                        .ifPresent(
                            finalItem -> {
                                CheckResult checkResult = tuple.getFirst();
                                Inventory inventory = tuple.getSecond();
                                ItemStack itemStack = inventory.peek().get();
                                inventory.set(finalItem);
                                TextUtil
                                    .prepareMessage(
                                        Triggers.JOIN,
                                        TextUtil.getDisplayName(itemStack),
                                        TextUtil.getDisplayName(finalItem),
                                        ((CheckResult.Banned) checkResult).getBanRules(),
                                        checkResult.isUpdateNeeded()
                                    )
                                    .forEach(player::sendMessage);
                            }
                        )
            );
    }
}
