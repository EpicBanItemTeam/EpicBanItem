package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryResult;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.*;

/**
 * @author EBI
 */
@NonnullByDefault
public class CommandQuery extends AbstractCommand {
    static Map<String, String> histories = new HashMap<>();

    public CommandQuery() {
        super("query", "q");
    }

    @Override
    public CommandElement getArgument() {
        return flags().flag("l").buildWith(optional(remainingRawJoinedStrings(Text.of("query-rule"))));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String id = src.getIdentifier();
        boolean lookAtBlock = args.hasAny("l");
        DataContainer nbt = toNbt(src, lookAtBlock);
        String rule = args.<String>getOne("query-rule").orElse(histories.getOrDefault(id, "{}"));
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
            histories.put(id, rule);
        } catch (Exception e) {
            EpicBanItem.logger.error(getMessage("error").toPlain(), e);
            throw new CommandException(Text.of(getMessage("error"), e.toString()));
        }
        return CommandResult.success();
    }

    private DataContainer toNbt(CommandSource src, boolean lookAtBlock) throws CommandException {
        if (lookAtBlock) {
            Optional<BlockSnapshot> b = CommandCreate.getBlockLookAt(src).map(Location::createSnapshot);
            return b.flatMap(NbtTagDataUtil::toNbt).orElseThrow(() -> new CommandException(getMessage("noBlock")));
        } else {
            Optional<ItemStack> i = CommandCreate.getItemInHand(src).map(Tuple::getSecond);
            return i.map(NbtTagDataUtil::toNbt).orElseThrow(() -> new CommandException(getMessage("noItem")));
        }
    }
}
