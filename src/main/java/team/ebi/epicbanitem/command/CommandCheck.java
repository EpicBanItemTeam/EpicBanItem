package team.ebi.epicbanitem.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import team.ebi.epicbanitem.api.CheckRuleTrigger;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.check.Triggers;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@Singleton
public class CommandCheck extends AbstractCommand {
    private static Map<String, CheckRule> checkRuleContext = new LinkedHashMap<>();

    @Inject
    private CheckRuleService service;

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
        return GenericArguments.flags().flag("l").buildWith(GenericArguments.none());
    }

    @Override
    @Nonnull
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        boolean lookAt = args.hasAny("l");
        checkRuleContext.clear();
        if (lookAt) {
            Optional<BlockSnapshot> optional = CommandCreate.getBlockLookAt(src);
            BlockSnapshot blockSnapshot = optional.orElseThrow(() -> new CommandException(getMessage("noBlock")));
            World world = ((Locatable) src).getWorld();
            for (CheckRuleTrigger trigger : Triggers.getTriggers().values()) {
                service.check(blockSnapshot, world, trigger, null);
            }
        } else {
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
