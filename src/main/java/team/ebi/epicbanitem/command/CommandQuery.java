package team.ebi.epicbanitem.command;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.util.NbtTagDataUtil;
import team.ebi.epicbanitem.util.TextUtil;
import team.ebi.epicbanitem.util.nbt.QueryExpression;
import team.ebi.epicbanitem.util.nbt.QueryResult;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public class CommandQuery extends AbstractCommand {
    static Cache<String, String> histories = Caffeine.newBuilder().softValues().expireAfterAccess(5, TimeUnit.MINUTES).build();

    @Inject
    Logger logger;

    CommandQuery() {
        super("query", "q");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments
            .flags()
            .flag("l")
            .setUnknownLongFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
            .setUnknownShortFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
            .buildWith(GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of("query-rule"))));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String id = src.getIdentifier();
        boolean lookAtBlock = args.hasAny("l");
        boolean[] unusedHistory = { true };
        String rule = args
            .<String>getOne("query-rule")
            .orElseGet(
                () -> {
                    unusedHistory[0] = false;
                    return histories.get(id, key -> "{}");
                }
            );
        try {
            DataContainer nbt;
            Translation name;
            Text titleText;
            if (lookAtBlock) {
                Optional<BlockSnapshot> optional = CommandCreate.getBlockLookAt(src);
                BlockSnapshot b = optional.orElseThrow(() -> new CommandException(getMessage("noBlock")));
                nbt = NbtTagDataUtil.toNbt(b);
                name = b.getState().getType().getTranslation();
                titleText = Text.of(name);
            } else {
                Optional<Tuple<HandType, ItemStack>> optional = CommandCreate.getItemInHand(src);
                Tuple<HandType, ItemStack> i = optional.orElseThrow(() -> new CommandException(getMessage("noItem")));
                nbt = NbtTagDataUtil.toNbt(i.getSecond());
                name = i.getSecond().getTranslation();
                titleText = Text.builder(name).onHover(TextActions.showItem(i.getSecond().createSnapshot())).build();
            }
            QueryExpression query = new QueryExpression(TextUtil.serializeStringToConfigNode(rule));
            DataQuery idQuery = DataQuery.of("id");
            Optional<QueryResult> result = query.query(DataQuery.of(), nbt);
            Optional<Tristate> tristate = nbt.getString(idQuery).map(s -> query.filterString(idQuery, s));
            if (result.isPresent()) {
                Text text = Text.of("id -> " + tristate + "\n" + result.get().toString());
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
                    .contents(TextUtil.serializeNbtToString(nbt, result.get()))
                    .sendTo(src);
            } else {
                Text text = Text.of("id -> " + tristate);
                src.sendMessage(getMessage("failed").toBuilder().onHover(TextActions.showText(text)).build());
            }

            if (unusedHistory[0]) {
                histories.put(id, rule);
            }
            return CommandResult.success();
        } catch (Exception e) {
            logger.error(getMessage("error").toPlain(), e);
            throw new CommandException(Text.of(getMessage("error"), e.getMessage()));
        }
    }
}
