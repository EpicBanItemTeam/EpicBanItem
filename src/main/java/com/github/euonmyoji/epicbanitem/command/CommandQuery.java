package com.github.euonmyoji.epicbanitem.command;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class CommandQuery extends AbstractCommand {
    static Cache<String, String> histories = Caffeine.newBuilder()
            .softValues()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    CommandQuery() {
        super("query", "q");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.flags().flag("l")
                .setUnknownLongFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
                .setUnknownShortFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
                .buildWith(GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of("query-rule"))));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String id = src.getIdentifier();
        boolean lookAtBlock = args.hasAny("l");
        boolean[] unusedHistory = {true};
        String rule = args.<String>getOne("query-rule")
                .orElseGet(() -> {
                    unusedHistory[0] = false;
                    return histories.get(id, (key) -> "{}");
                });
        try {
            DataContainer nbt;
            if (lookAtBlock) {
                Optional<BlockSnapshot> optional = CommandCreate.getBlockLookAt(src);
                BlockSnapshot b = optional.orElseThrow(() -> new CommandException(getMessage("noBlock")));
                nbt = NbtTagDataUtil.toNbt(b);
            } else {
                Optional<Tuple<HandType, ItemStack>> optional = CommandCreate.getItemInHand(src);
                Tuple<HandType, ItemStack> i = optional.orElseThrow(() -> new CommandException(getMessage("noItem")));
                nbt = NbtTagDataUtil.toNbt(i.getSecond());
            }
            QueryExpression query = new QueryExpression(TextUtil.serializeStringToConfigNode(rule));

            Optional<QueryResult> result = query.query(DataQuery.of(), nbt);
            if (result.isPresent()) {
                LiteralText text = Text.of(result.get().toString());
                Text.Builder prefix = getMessage("succeed").toBuilder().onHover(TextActions.showText(text));
                src.sendMessage(Text.of(prefix.build(), TextUtil.serializeNbtToString(nbt, result.get())));
            } else {
                src.sendMessage(getMessage("failed"));
            }

            if (unusedHistory[0]) {
                histories.put(id, rule);
            }
            return CommandResult.success();
        } catch (Exception e) {
            EpicBanItem.getLogger().error(getMessage("error").toPlain(), e);
            throw new CommandException(Text.of(getMessage("error"), e.getMessage()));
        }
    }

}
