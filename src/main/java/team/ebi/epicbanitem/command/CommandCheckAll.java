package team.ebi.epicbanitem.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.plugin.meta.util.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.CheckResult;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.check.Triggers;
import team.ebi.epicbanitem.command.arg.EpicBanItemArgs;
import team.ebi.epicbanitem.util.NbtTagDataUtil;
import team.ebi.epicbanitem.util.TextUtil;

import java.util.List;
import java.util.Optional;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public class CommandCheckAll extends AbstractCommand {
    @Inject
    private CheckRuleService service;

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

        src.sendMessage(Text.of(TextColors.GREEN, EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.command.checkall.success")));
        return CommandResult.success();
    }

    @SuppressWarnings("DuplicatedCode")
    private void replaceItems(Player player, Iterable<Tuple<CheckResult, Slot>> tuples) {
        for (Tuple<CheckResult, Slot> tuple : tuples) {
            CheckResult result = tuple.getFirst();
            if (result.isBanned()) {
                Optional<DataContainer> viewOptional = result.getFinalView();
                if (viewOptional.isPresent()) {
                    Slot slot = tuple.getSecond();
                    ItemStack item = slot.peek().orElse(ItemStack.empty());
                    ItemStack finalItem = NbtTagDataUtil.toItemStack(viewOptional.get(), item.getQuantity());

                    slot.set(finalItem);

                    Text itemName = TextUtil.getDisplayName(item);
                    Text finalItemName = TextUtil.getDisplayName(finalItem);
                    List<Tuple<Text, Optional<String>>> banRules = ((CheckResult.Banned) result).getBanRules();

                    TextUtil.prepareMessage(Triggers.JOIN, itemName, finalItemName, banRules, result.isUpdateNeeded()).forEach(player::sendMessage);
                }
            }
        }
    }
}
