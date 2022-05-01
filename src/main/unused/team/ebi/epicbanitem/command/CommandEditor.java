package team.ebi.epicbanitem.command;

import com.google.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.check.Triggers;
import team.ebi.epicbanitem.command.arg.ArgRangeInteger;
import team.ebi.epicbanitem.command.arg.EpicBanItemArgs;
import team.ebi.epicbanitem.ui.Button;
import team.ebi.epicbanitem.ui.ChatView;
import team.ebi.epicbanitem.ui.FixedTextElement;
import team.ebi.epicbanitem.ui.InputRequestElement;
import team.ebi.epicbanitem.ui.JoiningLine;
import team.ebi.epicbanitem.ui.SimpleLine;
import team.ebi.epicbanitem.ui.SwitchButton;
import team.ebi.epicbanitem.ui.TextLine;
import team.ebi.epicbanitem.ui.TranslateLine;
import team.ebi.epicbanitem.ui.UiTextElement;
import team.ebi.epicbanitem.ui.VariableHeightLines;
import team.ebi.epicbanitem.util.TextUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public class CommandEditor extends AbstractCommand {
    private static Map<UUID, Editor> editorMap = new HashMap<>();

    CommandEditor() {
        super("editor");
    }

    public static void add(Player player, CheckRuleLocation location, boolean sendMessage) {
        Editor editor = new Editor(player.getUniqueId(), location);
        editorMap.put(player.getUniqueId(), editor);
        if (sendMessage) {
            editor.resend();
        }
    }

    public static void add(Player player, CheckRuleLocation location, ConfigurationNode queryNode, boolean sendMessage) {
        Editor editor = new Editor(player.getUniqueId(), location);
        editor.ruleBuilder.queryNode(queryNode);
        editor.ruleBuilder.updateNode(CheckRule.getDefaultUpdateNode());
        editorMap.put(player.getUniqueId(), editor);
        if (sendMessage) {
            editor.resend();
        }
    }

    public static void add(Player player, CheckRule origin, boolean sendMessage) {
        Editor editor = new Editor(player.getUniqueId(), origin);
        editorMap.put(player.getUniqueId(), editor);
        if (sendMessage) {
            editor.resend();
        }
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(getMessage("unsupportedSource"));
        }
        Player player = (Player) src;
        Editor editor = editorMap.get(player.getUniqueId());
        if (editor == null) {
            src.sendMessage(getMessage("useOtherFirst"));
        } else {
            editor.resend();
        }
        return CommandResult.success();
    }

    @SuppressWarnings("SameParameterValue")
    private static class Editor {
        private UUID owner;

        @Nullable
        private CheckRule origin;

        private CheckRule.Builder ruleBuilder;

        private ChatView chatView;

        private Editor(UUID owner, CheckRuleLocation location) {
            this.owner = owner;
            this.ruleBuilder = CheckRule.builder(location);
            chatView = createView();
        }

        private Editor(UUID owner, CheckRule origin) {
            this.owner = owner;
            this.origin = origin;
            this.ruleBuilder = CheckRule.builder(origin);
            chatView = createView();
        }

        private ChatView createView() {
            List<TextLine> header = new ArrayList<>();
            header.add(new SimpleLine(Collections.singletonList(new FixedTextElement(getMessage("header")))));
            List<TextLine> context = new ArrayList<>();
            context.add(ruleName());
            context.add(priority());
            //worlds
            context.add(tristateTitle(ruleBuilder::getWorldDefaultSetting, ruleBuilder::worldDefaultSetting, "worlds"));
            Set<String> worlds = new TreeSet<>(ruleBuilder.getWorldSettings().keySet());
            Sponge.getServer().getAllWorldProperties().stream().map(WorldProperties::getWorldName).forEach(worlds::add);
            context.add(
                tristateElements(
                    worlds,
                    ruleBuilder.getWorldSettings(),
                    ruleBuilder::getWorldDefaultSetting,
                    EpicBanItem.getSettings()::isWorldDefaultEnabled,
                    Text::builder
                )
            );
            //triggers
            context.add(tristateTitle(ruleBuilder::getTriggerDefaultSetting, ruleBuilder::triggerDefaultSetting, "triggers"));
            context.add(
                tristateElements(
                    Triggers.getTriggers().values(),
                    ruleBuilder.getTriggerSettings(),
                    ruleBuilder::getTriggerDefaultSetting,
                    EpicBanItem.getSettings()::isTriggerDefaultEnabled,
                    t -> t.toText().toBuilder()
                )
            );
            //query
            context.add(query());
            //update
            context.add(update());
            context.add(customInfo());
            List<TextLine> footer = new ArrayList<>();
            footer.add(save());
            footer.add(new FixedTextElement(getMessage("header")));
            return new ChatView(20, header, new VariableHeightLines(context), footer);
        }

        private TextLine ruleName() {
            return new TranslateLine(
                new InputRequestElement<CheckRuleLocation>(
                    () -> addHoverMessage(Text.builder(ruleBuilder.getName().map(CheckRuleLocation::toString).orElse("")).color(TextColors.BLUE), getMessage("click")),
                    ruleBuilder::getName,
                    updateAndResend(ruleBuilder::name),
                    EpicBanItemArgs.patternString(Text.of("name"), CheckRuleLocation.NAME_PATTERN)
                ),
                t -> getMessage("name", "name", t)
            );
        }

        private TextLine priority() {
            return new TranslateLine(
                new InputRequestElement<>(
                    () -> addHoverMessage(Text.builder(String.valueOf(ruleBuilder.getPriority())).color(TextColors.BLUE), getMessage("click")),
                    ruleBuilder::getPriority,
                    updateAndResend(ruleBuilder::priority),
                    ArgRangeInteger.range(Text.of("priority"), 0, 9)
                ),
                t -> getMessage("priority", "priority", t)
            );
        }

        private TextLine tristateTitle(Supplier<Tristate> mode, Consumer<Tristate> update, String messageKey) {
            return new TranslateLine(
                new SwitchButton<>(
                    () ->
                        updateFormat(getModeDisplayName(mode.get()).toBuilder(), mode.get())
                            .onHover(TextActions.showText(Text.of(getModeDisplayName(mode.get()), Text.NEW_LINE, getModeDescription(mode.get())))),
                    mode.get(),
                    Arrays.asList(Tristate.values()),
                    updateAndResend(update),
                    to -> getMessage("tristate.click", "to", getModeDisplayName(to))
                ),
                t -> getMessage(messageKey, "mode", t)
            );
        }

        private <T> TextLine tristateElements(
            Collection<T> allT,
            Map<T, Boolean> settingsMap,
            Supplier<Tristate> getRuleDefault,
            Function<T, Boolean> getGlobalDefault,
            Function<T, Text.Builder> toTextBuilder
        ) {
            List<UiTextElement> elements = new ArrayList<>();
            Function<T, Boolean> getDefault = world -> {
                Tristate worldSetting = getRuleDefault.get();
                return worldSetting == Tristate.UNDEFINED ? getGlobalDefault.apply(world) : worldSetting.asBoolean();
            };
            Function<T, Boolean> getWorldSetting = world -> settingsMap.getOrDefault(world, getDefault.apply(world));
            for (T t : allT) {
                elements.add(
                    new SwitchButton<>(
                        () -> updateFormat(toTextBuilder.apply(t), getWorldSetting.apply(t), settingsMap.containsKey(t)),
                        toTristate(settingsMap.get(t)),
                        Arrays.asList(Tristate.values()),
                        updateAndResend(to -> updateMap(settingsMap, t, to)),
                        to ->
                            Text.of(
                                getMessage("tristate.display", "value", toText(settingsMap.get(t)), "default", toText(getDefault.apply(t))),
                                Text.NEW_LINE,
                                getMessage("tristate.click", "to", toText(to))
                            )
                    )
                );
            }
            return new JoiningLine(elements, new FixedTextElement(Text.of("  ")));
        }

        private TextLine query() {
            return new TranslateLine(
                p -> Text.joinWith(Text.builder("  ").style(TextStyles.RESET).build(), genQueryTexts()),
                t -> getMessage("query", "options", t)
            );
        }

        private TextLine update() {
            return new TranslateLine(
                p -> Text.joinWith(Text.builder("  ").style(TextStyles.RESET).build(), genUpdateTexts()),
                t -> getMessage("update", "options", t)
            );
        }

        private TextLine customInfo() {
            BooleanSupplier isCustom = () -> ruleBuilder.getCustomMessageString() != null;
            UiTextElement edit = new InputRequestElement<>(
                () ->
                    getMessage(isCustom.getAsBoolean() ? "info.edit" : "info.set")
                        .toBuilder()
                        .onHover(
                            isCustom.getAsBoolean()
                                ? TextActions.showText(
                                    getMessage("info.edit.hover", "messageString", Objects.requireNonNull(ruleBuilder.getCustomMessageString()))
                                )
                                : TextActions.showText(getMessage("info.set.hover"))
                        ),
                () -> ruleBuilder.getCustomMessageString(),
                GenericArguments.optional(GenericArguments.string(Text.of("customMessage"))),
                (src, args) -> {
                    ruleBuilder.customMessage(args.<String>getOne("customMessage").orElse(""));
                    resend();
                    return CommandResult.success();
                }
            );
            Supplier<UiTextElement> unset = () ->
                isCustom.getAsBoolean()
                    ? new Button(() -> getMessage("info.unset").toBuilder().onHover(TextActions.showText(getMessage("info.unset.hover")))) {

                        @Override
                        public void onClick(CommandSource source) {
                            ruleBuilder.customMessage(null);
                            resend();
                        }
                    }
                    : new FixedTextElement(Text.EMPTY);
            return viewer -> getMessage("info", "edit", edit.toText(viewer), "unset", unset.get().toText(viewer));
        }

        private TextLine save() {
            return new SimpleLine(
                Collections.singletonList(
                    new Button(getMessage("save")::toBuilder) {

                        @Override
                        public void onClick(CommandSource source) {
                            CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
                            CheckRule rule = ruleBuilder.build();

                            // remove th origin one.
                            if (origin != null) {
                                // TODO: 2018/11/24 Is the rule with that name the rule we get on created the editor.
                                service.removeRule(origin).join();
                            }
                            // TODO: 2018/11/25 Warn on no id matches ?
                            // Optional<String> id = Optional.ofNullable(ruleBuilder.getQueryNode().getNode("id").getString()); id is unused ??
                            service
                                .appendRule(rule)
                                .whenComplete(
                                    (aBoolean, throwable) -> getOwner().ifPresent(player -> createResultView(aBoolean, throwable).showTo(player))
                                );
                            editorMap.remove(owner);
                            CommandCallback.clear(owner);
                        }
                    }
                )
            );
        }

        private ChatView createResultView(@Nullable Boolean b, @Nullable Throwable t) {
            List<TextLine> header = new ArrayList<>();
            header.add(new FixedTextElement(getMessage("header")));
            List<TextLine> context = new ArrayList<>();
            if (b != null) {
                if (b) {
                    context.add(new FixedTextElement(getMessage("saved")));
                } else {
                    context.add(new FixedTextElement(getMessage("existed")));
                }
            } else {
                context.add(new FixedTextElement(getMessage("exception", "e", String.valueOf(t))));
            }
            return new ChatView(20, header, new VariableHeightLines(context), header);
        }

        private <T> Consumer<T> updateAndResend(Consumer<T> update) {
            return update.andThen(t -> resend());
        }

        private Optional<Player> getOwner() {
            return Sponge.getServer().getPlayer(owner);
        }

        private static Text getMessage(String key) {
            return EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.command.editor." + key);
        }

        private static Text getMessage(String key, String k1, Object v1) {
            return EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.command.editor." + key, Tuple.of(k1, v1));
        }

        private static Text getMessage(String key, String k1, Object v1, String k2, Object v2) {
            return EpicBanItem.getLocaleService().getTextWithFallback("epicbanitem.command.editor." + key, Tuple.of(k1, v1), Tuple.of(k2, v2));
        }

        private static String toString(@Nullable Boolean b) {
            return toTristate(b).toString();
        }

        private static Text getModeDisplayName(Tristate mode) {
            return getMessage("mode." + mode.toString().toLowerCase(Locale.ROOT));
        }

        private static Text getModeDescription(Tristate mode) {
            return getMessage("mode." + mode.toString().toLowerCase(Locale.ROOT) + ".description");
        }

        private static <T> void updateMap(Map<T, Boolean> map, T key, Tristate value) {
            if (value == Tristate.UNDEFINED) {
                map.remove(key);
            } else {
                map.put(key, value.asBoolean());
            }
        }

        private static Tristate toTristate(@Nullable Boolean b) {
            if (b == null) {
                return Tristate.UNDEFINED;
            } else {
                return Tristate.fromBoolean(b);
            }
        }

        private static Text toText(Tristate tristate) {
            return getMessage("tristate." + tristate.toString().toLowerCase(Locale.ROOT));
        }

        private static Text toText(Boolean b) {
            return toText(toTristate(b));
        }

        /*
         * enable   - Green
         * disable  - Red
         * undefine - Italic
         */
        private static Text.Builder updateFormat(Text.Builder builder, boolean enable, boolean set) {
            TextFormat format = TextFormat.of(enable ? TextColors.GREEN : TextColors.RED, builder.getStyle().italic(!set));
            return builder.format(format);
        }

        private static Text.Builder updateFormat(Text.Builder builder, Tristate tristate) {
            TextColor color;
            switch (tristate) {
                case TRUE:
                    color = TextColors.GREEN;
                    break;
                case FALSE:
                    color = TextColors.RED;
                    break;
                case UNDEFINED:
                default:
                    color = TextColors.GRAY;
            }
            TextFormat format = TextFormat.of(color, builder.getStyle());
            return builder.format(format);
        }

        private static Text.Builder addHoverMessage(Text.Builder builder, Text text) {
            Optional<HoverAction<?>> optionalHoverAction = builder.getHoverAction();
            if (optionalHoverAction.isPresent()) {
                HoverAction<?> hoverAction = optionalHoverAction.get();
                if (hoverAction instanceof HoverAction.ShowText) {
                    Text origin = ((HoverAction.ShowText) hoverAction).getResult();
                    text = origin.toBuilder().append(Text.NEW_LINE, text).build();
                }
            }
            return builder.onHover(TextActions.showText(text));
        }

        private void resend() {
            Optional<Player> optionalPlayer = getOwner();
            if (!optionalPlayer.isPresent()) {
                return;
            }
            CommandCallback.clear(owner);
            Player player = optionalPlayer.get();
            chatView.showTo(player);
        }

        private Text formatNode(String display, Text description, @Nullable ConfigurationNode node, String key) {
            String ebi = CommandEbi.COMMAND_PREFIX;
            Text.Builder builder = Text.builder(display);
            String nodeString;
            String suggestString;
            if (node == null) {
                nodeString = "<null>";
                suggestString = "";
            } else if (node.getValue() == null) {
                nodeString = suggestString = "{}";
            } else {
                try {
                    nodeString = TextUtil.deserializeConfigNodeToString(node);
                    suggestString = TextUtil.deserializeConfigNodeToPlanString(node);
                } catch (IOException e) {
                    EpicBanItem.getLogger().error("Error on deserialize ConfigNode to String", e);
                    nodeString = suggestString = "error";
                }
            }
            Text hover = Text.of(description, Text.NEW_LINE, Text.NEW_LINE, nodeString, Text.NEW_LINE, getMessage("click"));
            builder.onHover(TextActions.showText(hover));
            builder.onClick(TextActions.suggestCommand(String.format("/%s cb %s %s", ebi, key, suggestString)));
            return builder.toText();
        }

        private List<Text> genQueryTexts() {
            String queryKey = CommandCallback.add(
                owner,
                GenericArguments.remainingRawJoinedStrings(Text.of("query-rule")),
                (src, args) -> {
                    String rule = args.<String>getOne("query-rule").orElseThrow(NoClassDefFoundError::new);
                    try {
                        ConfigurationNode node = TextUtil.serializeStringToConfigNode(rule);
                        ruleBuilder.queryNode(node);
                    } catch (IOException e) {
                        throw handleException(src, Text.of("Unexpected error."), e);
                    }
                    resend();
                    return CommandResult.success();
                }
            );
            List<Text> queries = new ArrayList<>();
            queries.add(formatNode(getMessage("custom").toPlain(), getMessage("customQuery"), ruleBuilder.getQueryNode(), queryKey));
            getOwner()
                .ifPresent(
                    p -> {
                        String s = CommandQuery.histories.getIfPresent(p.getIdentifier());
                        if (s != null) {
                            try {
                                ConfigurationNode node = TextUtil.serializeStringToConfigNode(s);
                                queries.add(formatNode(getMessage("history").toPlain(), getMessage("historyQuery"), node, queryKey));
                            } catch (IOException ignore) {}
                        }
                    }
                );
            if (origin != null) {
                queries.add(formatNode(getMessage("origin").toPlain(), getMessage("originQuery"), origin.getQueryNode(), queryKey));
            }
            return queries;
        }

        private List<Text> genUpdateTexts() {
            String updateKey = CommandCallback.add(
                owner,
                GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of("update-rule"))),
                (src, args) -> {
                    Optional<String> rule = args.getOne("update-rule");
                    if (rule.isPresent()) {
                        try {
                            ConfigurationNode node = TextUtil.serializeStringToConfigNode(rule.get());
                            ruleBuilder.updateNode(node);
                        } catch (IOException e) {
                            throw handleException(src, Text.of("Unexpected error."), e);
                        }
                    } else {
                        ruleBuilder.updateNode(null);
                    }
                    resend();
                    return CommandResult.success();
                }
            );
            List<Text> updates = new ArrayList<>();
            updates.add(formatNode(getMessage("custom").toPlain(), getMessage("customUpdate"), ruleBuilder.getUpdateNode(), updateKey));
            updates.add(formatNode(getMessage("default").toPlain(), getMessage("defaultUpdate"), CheckRule.getDefaultUpdateNode(), updateKey));
            updates.add(formatNode(getMessage("empty").toPlain(), getMessage("emptyUpdate"), null, updateKey));

            // TODO: 2018/11/24 History of update command
            if (origin != null) {
                updates.add(formatNode(getMessage("origin").toPlain(), getMessage("customUpdate"), origin.getUpdateNode(), updateKey));
            }
            return updates;
        }
    }
}
