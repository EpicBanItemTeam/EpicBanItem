package team.ebi.epicbanitem.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
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
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.BlockChangeFlags;
import team.ebi.epicbanitem.util.NbtTagDataUtil;
import team.ebi.epicbanitem.util.TextUtil;
import team.ebi.epicbanitem.util.nbt.QueryExpression;
import team.ebi.epicbanitem.util.nbt.QueryResult;
import team.ebi.epicbanitem.util.nbt.UpdateExpression;
import team.ebi.epicbanitem.util.nbt.UpdateResult;

import java.util.Optional;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public class CommandUpdate extends AbstractCommand {
    @Inject
    private Logger logger;

    CommandUpdate() {
        super("update", "u");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments
            .flags()
            .flag("l")
            .setUnknownLongFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
            .setUnknownShortFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
            .buildWith(GenericArguments.remainingRawJoinedStrings(Text.of("update-rule")));
    }

    @SuppressWarnings("Duplicates for lj")
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String id = src.getIdentifier();
        boolean lookAtBlock = args.hasAny("l");
        String updateRule = args.<String>getOne("update-rule").orElseThrow(NoSuchFieldError::new);
        String queryRule = CommandQuery.histories.get(id, key -> "{}");
        try {
            Text titleText;
            Translation name;
            DataContainer nbt;
            QueryResult queryResult;
            UpdateResult updateResult;
            QueryExpression query = new QueryExpression(TextUtil.serializeStringToConfigNode(queryRule));
            UpdateExpression update = new UpdateExpression(TextUtil.serializeStringToConfigNode(updateRule));
            if (lookAtBlock) {
                Optional<BlockSnapshot> optional = CommandCreate.getBlockLookAt(src);
                BlockSnapshot b = optional.orElseThrow(() -> new CommandException(getMessage("noBlock")));

                nbt = NbtTagDataUtil.toNbt(b);
                queryResult = query.query(DataQuery.of(), nbt).orElse(QueryResult.success().orElseThrow(NoSuchFieldError::new));
                updateResult = update.update(queryResult, nbt);
                updateResult.apply(nbt);
                b = NbtTagDataUtil.toBlockSnapshot(nbt, b.getWorldUniqueId());
                b.restore(true, BlockChangeFlags.NONE);
                // TODO: should it really be none?

                name = b.getState().getType().getTranslation();
                titleText = Text.of(name);
            } else {
                Optional<Tuple<HandType, ItemStack>> optional = CommandCreate.getItemInHand(src);
                Tuple<HandType, ItemStack> i = optional.orElseThrow(() -> new CommandException(getMessage("noItem")));

                nbt = NbtTagDataUtil.toNbt(i.getSecond());
                queryResult = query.query(DataQuery.of(), nbt).orElse(QueryResult.success().orElseThrow(NoSuchFieldError::new));
                updateResult = update.update(queryResult, nbt);
                updateResult.apply(nbt);
                i = Tuple.of(i.getFirst(), NbtTagDataUtil.toItemStack(nbt, i.getSecond().getQuantity()));
                CommandCreate.setItemInHand(src, i);

                name = i.getSecond().getTranslation();
                titleText = Text.builder(name).onHover(TextActions.showItem(i.getSecond().createSnapshot())).build();
            }

            LiteralText text = Text.of(updateResult.toString());
            Text.Builder prefix = Text
                    .builder()
                    .append(getMessage("succeed"))
                    .append(Text.of(name))
                    .onHover(TextActions.showText(text))
                    .append(Text.NEW_LINE);
            PaginationList
                    .builder()
                    .title(titleText)
                    .header(prefix.build())
                    .padding(Text.of(TextColors.GREEN, "-"))
                    .contents(TextUtil.serializeNbtToString(nbt, queryResult))
                    .sendTo(src);
            return CommandResult.success();
        } catch (Exception e) {
            logger.error(getMessage("error").toPlain(), e);
            throw new CommandException(Text.of(getMessage("error"), e.getMessage()));
        }
    }
}
