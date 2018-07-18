package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryResult;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.remainingRawJoinedStrings;

@NonnullByDefault
public class CommandQuery extends AbstractCommand {
    static Map<UUID, String> histories = new HashMap<>();

    public CommandQuery() {
        super("query", "q");
    }

    @Override
    public CommandElement getArgument() {
        return optional(remainingRawJoinedStrings(Text.of("query-rule")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<ItemStack> itemStackOptional = Optional.empty();
        if (src instanceof ArmorEquipable) {
            for (HandType type : Sponge.getRegistry().getAllOf(HandType.class)) {
                itemStackOptional = ((ArmorEquipable) src).getItemInHand(type);
                if (itemStackOptional.isPresent()) {
                    break;
                }
            }
        }
        if (!itemStackOptional.isPresent()) {
            throw new CommandException(getMessage("noItem"));
        }
        UUID uuid = ((ArmorEquipable) src).getUniqueId();
        DataContainer nbt = NbtTagDataUtil.toNbt(itemStackOptional.get());
        // noinspection ConstantConditions
        String rule = args.<String>getOne("query-rule").orElse(histories.getOrDefault(uuid, "{}"));
        try {
            QueryExpression query = new QueryExpression(TextUtil.serializeStringToConfigNode(rule));
            Optional<QueryResult> result = query.query(DataQuery.of(), nbt);
            if (result.isPresent()) {
                LiteralText text = Text.of(result.get().toString());
                Text.Builder prefix = Text.builder().append(getMessage("succeed")).onHover(TextActions.showText(text));
                src.sendMessage(Text.of(prefix.build(), TextUtil.serializeNbtToString(nbt, result.get())));
            } else {
                src.sendMessage(getMessage("failed"));
            }
            histories.put(uuid, rule);
        } catch (Exception e) {
            EpicBanItem.logger.error(getMessage("error").toPlain(), e);
            throw new CommandException(Text.of(getMessage("error"), e.toString()));
        }
        return CommandResult.success();
    }

}
