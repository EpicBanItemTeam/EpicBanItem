package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryResult;
import com.github.euonmyoji.epicbanitem.util.nbt.UpdateExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.UpdateResult;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.CommandFlags;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.BlockChangeFlags;

import java.util.Optional;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class CommandUpdate extends AbstractCommand {
    CommandUpdate() {
        super("update", "u");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.flags().flag("l")
                .setUnknownLongFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
                .setUnknownShortFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
                .buildWith(GenericArguments.remainingRawJoinedStrings(Text.of("update-rule")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String id = src.getIdentifier();
        boolean lookAtBlock = args.hasAny("l");
        // noinspection ConstantConditions
        String updateRule = args.<String>getOne("update-rule").get();
        String queryRule = CommandQuery.histories.getOrDefault(id, "{}");
        try {
            if (lookAtBlock) {
                Optional<BlockSnapshot> optional = CommandCreate.getBlockLookAt(src);
                BlockSnapshot b = optional.orElseThrow(() -> new CommandException(getMessage("noBlock")));

                DataContainer nbt = NbtTagDataUtil.toNbt(b);
                QueryExpression query = new QueryExpression(TextUtil.serializeStringToConfigNode(queryRule));
                UpdateExpression update = new UpdateExpression(TextUtil.serializeStringToConfigNode(updateRule));

                // noinspection ConstantConditions
                QueryResult queryResult = query.query(DataQuery.of(), nbt).orElse(QueryResult.success().get());
                UpdateResult updateResult = update.update(queryResult, nbt);
                updateResult.apply(nbt);

                b = NbtTagDataUtil.toBlockSnapshot(nbt, b.getState(), b.getWorldUniqueId());
                b.restore(true, BlockChangeFlags.NONE); // TODO: should it really be none?

                LiteralText text = Text.of(updateResult.toString());
                Text.Builder prefix = getMessage("succeed").toBuilder().onHover(TextActions.showText(text));
                src.sendMessage(Text.of(prefix.build(), TextUtil.serializeNbtToString(nbt, queryResult)));
            } else {
                Optional<Tuple<HandType, ItemStack>> optional = CommandCreate.getItemInHand(src);
                Tuple<HandType, ItemStack> i = optional.orElseThrow(() -> new CommandException(getMessage("noItem")));

                DataContainer nbt = NbtTagDataUtil.toNbt(i.getSecond());
                QueryExpression query = new QueryExpression(TextUtil.serializeStringToConfigNode(queryRule));
                UpdateExpression update = new UpdateExpression(TextUtil.serializeStringToConfigNode(updateRule));

                // noinspection ConstantConditions
                QueryResult queryResult = query.query(DataQuery.of(), nbt).orElse(QueryResult.success().get());
                UpdateResult updateResult = update.update(queryResult, nbt);
                updateResult.apply(nbt);

                i = Tuple.of(i.getFirst(), NbtTagDataUtil.toItemStack(nbt, i.getSecond().getQuantity()));
                CommandCreate.setItemInHand(src, i);

                LiteralText text = Text.of(updateResult.toString());
                Text.Builder prefix = getMessage("succeed").toBuilder().onHover(TextActions.showText(text));
                src.sendMessage(Text.of(prefix.build(), TextUtil.serializeNbtToString(nbt, queryResult)));
            }
            return CommandResult.success();
        } catch (Exception e) {
            EpicBanItem.getLogger().error(getMessage("error").toPlain(), e);
            throw new CommandException(Text.of(getMessage("error"), e.getMessage()));
        }
    }
}
