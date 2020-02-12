package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import com.github.euonmyoji.epicbanitem.configuration.Settings;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class CommandEditor extends AbstractCommand {
    private static Map<UUID, Editor> editorMap = new HashMap<>();

    CommandEditor() {
        super("editor");
    }

    public static void add(Player player, String name, boolean sendMessage) {
        Editor editor = new Editor(player.getUniqueId(), name);
        editorMap.put(player.getUniqueId(), editor);
        if (sendMessage) {
            editor.resend();
        }
    }

    public static void add(Player player, String name, ConfigurationNode queryNode, boolean sendMessage) {
        Editor editor = new Editor(player.getUniqueId(), name);
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

        private Editor(UUID owner, String name) {
            this.owner = owner;
            this.ruleBuilder = CheckRule.builder(name);
        }

        private Editor(UUID owner, CheckRule origin) {
            this.owner = owner;
            this.origin = origin;
            this.ruleBuilder = CheckRule.builder(origin);
        }

        private static Text getMessage(String key) {
            return EpicBanItem.getMessages().getMessage("epicbanitem.command.editor." + key);
        }

        private static Text getMessage(String key, String k1, Object v1) {
            return EpicBanItem.getMessages().getMessage("epicbanitem.command.editor." + key, k1, v1);
        }

        private static Text getMessage(String key, String k1, Object v1, String k2, Object v2) {
            return EpicBanItem.getMessages().getMessage("epicbanitem.command.editor." + key, k1, v1, k2, v2);
        }

        private static String toString(@Nullable Boolean b) {
            return toTristate(b).toString();
        }

        /**
         * null->true->false->null
         *
         * @param b origin value
         * @return next value
         */
        @Nullable
        private static Boolean next(@Nullable Boolean b) {
            if (b == null) {
                return true;
            } else if (b) {
                return false;
            } else {
                return null;
            }
        }

        private static Tristate next(Tristate tristate) {
            if (tristate == Tristate.UNDEFINED) {
                return Tristate.TRUE;
            } else if (tristate.asBoolean()) {
                return Tristate.FALSE;
            } else {
                return Tristate.UNDEFINED;
            }
        }

        private static Tristate toTristate(@Nullable Boolean b) {
            if (b == null) {
                return Tristate.UNDEFINED;
            } else {
                return Tristate.fromBoolean(b);
            }
        }

        private void resend() {
            Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(owner);
            if (!optionalPlayer.isPresent()) {
                return;
            }
            CommandCallback.clear(owner);
            Player player = optionalPlayer.get();
            player.sendMessage(genText());
        }

        /**
         * enable   - Green
         * disable  - Red
         * undefine - Italic
         * clickable- Underline
         * <p>
         * CheckRule:{name}
         * Priority :{priority}
         * Triggers : White/Black list
         * {triggers}
         * Worlds   : White/Black list
         * {worlds}
         * QueryExpression : not editable
         * UpdateExpression: not editable
         * Custom Message  : Set/Edit (Unset)
         * <p>
         * Save
         */
        private Text genText() {
            //Builder appends: Header Name Priority
            Text.Builder builder = Text
                .builder()
                .append(getMessage("header"), Text.NEW_LINE)
                .append(
                    getMessage(
                        "name",
                        "name",
                        format(
                            ruleBuilder.getName(),
                            origin != null && !origin.getName().equals(ruleBuilder.getName()),
                            new Tuple<>(
                                EpicBanItemArgs.patternString(Text.of("name"), CheckRule.NAME_PATTERN),
                                (src, args) -> {
                                    ruleBuilder.name(args.<String>getOne("name").orElseThrow(NoSuchFieldError::new));
                                    resend();
                                    return CommandResult.success();
                                }
                            )
                        )
                    )
                )
                .append(Text.NEW_LINE)
                .append(
                    getMessage(
                        "priority",
                        "priority",
                        format(
                            ruleBuilder.getPriority(),
                            origin != null && origin.getPriority() != ruleBuilder.getPriority(),
                            new Tuple<>(
                                GenericArguments.integer(Text.of("priority")),
                                (src, args) -> {
                                    int priority = args.<Integer>getOne("priority").orElseThrow(NoSuchFieldError::new);
                                    if (priority < 0 || priority > 9) {
                                        throw new CommandException(getMessage("illegalPriority"));
                                    }
                                    ruleBuilder.priority(priority);
                                    resend();
                                    return CommandResult.success();
                                }
                            )
                        )
                    )
                )
                .append(Text.NEW_LINE);
            Settings settings = EpicBanItem.getSettings();
            boolean isOriginNull = origin == null;
            //Worlds
            Set<String> setWorlds = new HashSet<>(ruleBuilder.getWorldSettings().keySet());
            List<Text> worlds = new ArrayList<>();
            Tristate worldDefaultSetting = ruleBuilder.getWorldDefaultSetting();
            Function<String, Boolean> worldDefault = worldDefaultSetting == Tristate.UNDEFINED
                ? settings::isWorldDefaultEnabled
                : s -> worldDefaultSetting.asBoolean();
            checkAddRemove(
                worlds,
                worldDefault,
                setWorlds,
                isOriginNull ? null : origin.getWorldSettings(),
                ruleBuilder.getWorldSettings(),
                Sponge.getServer().getAllWorldProperties().stream().map(WorldProperties::getWorldName).collect(Collectors.toSet())
            );
            //worlds that not in the sponge data?
            checkAdd(worlds, worldDefault, origin != null ? origin.getWorldSettings() : null, ruleBuilder.getWorldSettings(), setWorlds);
            builder
                .append(
                    getMessage("worlds", "mode", formatMode(worldDefaultSetting, CheckRule::getWorldDefaultSetting, ruleBuilder::worldDefaultSetting))
                )
                .append(Text.NEW_LINE)
                .append(Text.joinWith(Text.builder("  ").style(TextStyles.RESET).build(), worlds))
                .append(Text.NEW_LINE);
            //Triggers
            Set<String> setTriggers = new HashSet<>(ruleBuilder.getTriggerSettings().keySet());
            List<Text> triggers = new ArrayList<>();
            Tristate triggerDefaultSetting = ruleBuilder.getTriggerDefaultSetting();
            Function<String, Boolean> triggerDefault = triggerDefaultSetting == Tristate.UNDEFINED
                ? settings::isTriggerDefaultEnabled
                : s -> triggerDefaultSetting.asBoolean();
            checkAddRemove(
                triggers,
                triggerDefault,
                setTriggers,
                isOriginNull ? null : origin.getTriggerSettings(),
                ruleBuilder.getTriggerSettings(),
                Triggers.getTriggers().keySet()
            );
            checkAdd(triggers, triggerDefault, isOriginNull ? null : origin.getTriggerSettings(), ruleBuilder.getTriggerSettings(), setTriggers);
            builder
                .append(
                    getMessage(
                        "triggers",
                        "mode",
                        formatMode(triggerDefaultSetting, CheckRule::getTriggerDefaultSetting, ruleBuilder::triggerDefaultSetting)
                    )
                )
                .append(Text.NEW_LINE)
                .append(Text.joinWith(Text.builder("  ").style(TextStyles.RESET).build(), triggers))
                .append(Text.NEW_LINE);
            //Query Updates
            builder
                .append(getMessage("query", "options", Text.joinWith(Text.builder("  ").style(TextStyles.RESET).build(), genQueryTexts())))
                .append(Text.NEW_LINE)
                .append(getMessage("update", "options", Text.joinWith(Text.builder("  ").style(TextStyles.RESET).build(), genUpdateTexts())))
                .append(Text.NEW_LINE);
            boolean isInfoSet = ruleBuilder.getCustomMessageString() != null;
            builder.append(
                getMessage(
                    "info",
                    "edit",
                    format(
                        getMessage(isInfoSet ? "info.edit" : "info.set"),
                        origin != null && !Objects.equals(origin.getCustomMessageString().orElse(null), ruleBuilder.getCustomMessageString()),
                        isInfoSet
                            ? getMessage("info.edit.hover", "messageString", ruleBuilder.getCustomMessageString())
                            : getMessage("info.set.hover"),
                        new Tuple<>(
                            GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("customMessage"))),
                            (src, args) -> {
                                ruleBuilder.customMessage(args.<String>getOne("customMessage").orElse(""));
                                resend();
                                return CommandResult.success();
                            }
                        )
                    ),
                    "unset",
                    isInfoSet
                        ? format(
                            getMessage("info.unset"),
                            false,
                            getMessage("info.unset.hover"),
                            new Tuple<>(
                                GenericArguments.none(),
                                (src, args) -> {
                                    ruleBuilder.customMessage(null);
                                    resend();
                                    return CommandResult.success();
                                }
                            )
                        )
                        : Text.EMPTY
                ),
                Text.NEW_LINE
            );
            //Save
            builder.append(
                getMessage("save")
                    .toBuilder()
                    .style(TextStyles.UNDERLINE, TextStyles.BOLD)
                    .color(TextColors.RED)
                    .onClick(
                        TextActions.runCommand(
                            String.format(
                                "/%s cb %s",
                                EpicBanItem.getMainCommandAlias(),
                                CommandCallback.add(
                                    owner,
                                    GenericArguments.none(),
                                    (src, args) -> {
                                        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
                                        CheckRule rule = ruleBuilder.build();
                                        // remove th origin one.
                                        if (origin != null) {
                                            // TODO: 2018/11/24 Is the rule with that name the rule we get on created the editor.
                                            service.removeRule(origin).join();
                                        }
                                        // TODO: 2018/11/25 Warn on no id matches ?
                                        //                                        Optional<String> id = Optional.ofNullable(ruleBuilder.getQueryNode().getNode("id").getString()); id is unused ??
                                        service
                                            .appendRule(rule)
                                            .thenRun(
                                                () ->
                                                    src.sendMessage(
                                                        Text.of(
                                                            Text.NEW_LINE,
                                                            Text.NEW_LINE,
                                                            Text.NEW_LINE,
                                                            Text.NEW_LINE,
                                                            Text.NEW_LINE,
                                                            getMessage("saved"),
                                                            Text.NEW_LINE
                                                        )
                                                    )
                                            );
                                        editorMap.remove(owner);
                                        CommandCallback.clear(owner);
                                        return CommandResult.success();
                                    }
                                )
                            )
                        )
                    )
                    .toText()
            );
            return builder.toText();
        }

        /* ********************************************************************************************************** */
        /* Gen the text helper                                                                                        */
        /* ********************************************************************************************************** */

        private Text format(Object value, boolean edited, Tuple<CommandElement, CommandExecutor> action) {
            return format(value, edited, null, action);
        }

        private Text format(Object value, boolean edited, @Nullable Text hover, Tuple<CommandElement, CommandExecutor> action) {
            String ebi = EpicBanItem.getMainCommandAlias();
            Text.Builder builder = Text.builder().append(Text.of(value));
            //Mark edited parts bold.
            builder.style(builder.getStyle().bold(edited));
            builder.color(TextColors.BLUE);
            if (action.getFirst().equals(GenericArguments.none())) {
                builder.onClick(TextActions.runCommand(String.format("/%s cb %s", ebi, CommandCallback.add(owner, action))));
            } else {
                builder.onClick(
                    TextActions.suggestCommand(
                        String.format(
                            "/%s cb %s %s",
                            ebi,
                            CommandCallback.add(owner, action),
                            value instanceof Text ? ((Text) value).toPlain() : String.valueOf(value)
                        )
                    )
                );
            }
            builder.onHover(TextActions.showText(hover == null ? getMessage("click") : hover));
            return builder.build();
        }

        private Text format(
            String text,
            Text hover,
            @Nullable Boolean vale,
            boolean defaultVale,
            boolean edited,
            Tuple<CommandElement, CommandExecutor> action
        ) {
            String ebi = EpicBanItem.getMainCommandAlias();
            Text.Builder builder = Text.builder(text);
            //Mark edited parts bold.
            builder.style(builder.getStyle().bold(edited).italic(Objects.isNull(vale)));
            builder.color((Objects.isNull(vale) ? defaultVale : vale) ? TextColors.GREEN : TextColors.RED);
            if (action.getFirst().equals(GenericArguments.none())) {
                builder.onClick(TextActions.runCommand(String.format("/%s cb %s", ebi, CommandCallback.add(owner, action))));
            } else {
                builder.onClick(TextActions.suggestCommand(String.format("/%s cb %s", ebi, CommandCallback.add(owner, action))));
            }
            Text.Builder tri = Text.builder();
            Text display = getMessage(
                "tristate.display",
                "value",
                getMessage("tristate." + toString(vale).toLowerCase()),
                "default",
                getMessage("tristate." + toString(defaultVale).toLowerCase())
            );
            Text click = getMessage("tristate.click", "to", getMessage("tristate." + toString(next(vale)).toLowerCase()));
            if (!hover.isEmpty()) {
                tri.append(hover, Text.NEW_LINE, Text.NEW_LINE);
            }
            tri.append(display, Text.NEW_LINE).append(click);
            builder.onHover(TextActions.showText(tri.build()));
            return builder.build();
        }

        private Text formatMode(Tristate tristate, Function<CheckRule, Tristate> getter, Consumer<Tristate> setter) {
            Text.Builder builder = getMessage("mode." + tristate.toString().toLowerCase()).toBuilder();
            Tristate to = next(tristate);
            boolean edited;
            if (origin == null) {
                edited = false;
            } else {
                edited = getter.apply(origin) != tristate;
            }
            builder.style(builder.getStyle().bold(edited).italic(false));
            builder.onClick(
                TextActions.runCommand(
                    String.format(
                        "/%s cb %s",
                        EpicBanItem.getMainCommandAlias(),
                        CommandCallback.add(
                            owner,
                            new Tuple<>(
                                GenericArguments.none(),
                                (src, args) -> {
                                    setter.accept(to);
                                    resend();
                                    return CommandResult.success();
                                }
                            )
                        )
                    )
                )
            );
            Text tri = Text
                .builder()
                .append(getMessage("mode." + tristate.toString().toLowerCase() + ".description"))
                .append(Text.NEW_LINE)
                .append(Text.NEW_LINE)
                .append(getMessage("tristate.click", "to", getMessage("mode." + to.toString().toLowerCase())))
                .build();
            builder.onHover(TextActions.showText(tri));
            return builder.build();
        }

        private Text formatNode(String display, Text description, @Nullable ConfigurationNode node, String key) {
            String ebi = EpicBanItem.getMainCommandAlias();
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
            Sponge
                .getServer()
                .getPlayer(owner)
                .ifPresent(
                    p -> {
                        String s = CommandQuery.histories.getIfPresent(p.getIdentifier());
                        if (s != null) {
                            try {
                                ConfigurationNode node = TextUtil.serializeStringToConfigNode(s);
                                queries.add(formatNode(getMessage("history").toPlain(), getMessage("historyQuery"), node, queryKey));
                            } catch (IOException e) {
                                //do nothing
                            }
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

        private void checkAdd(
            List<Text> listToAdd,
            Function<String, Boolean> defValue,
            @Nullable Map<String, Boolean> originEnableInfo,
            Map<String, Boolean> enableInfo,
            Iterable<String> names
        ) {
            for (String name : names) {
                checkAdd(listToAdd, defValue, name, originEnableInfo, enableInfo);
            }
        }

        private void checkAddRemove(
            List<Text> listToAdd,
            Function<String, Boolean> defValue,
            Collection<String> toRemove,
            @Nullable Map<String, Boolean> originEnableInfo,
            Map<String, Boolean> enableInfo,
            Iterable<String> names
        ) {
            for (String name : names) {
                checkAdd(listToAdd, defValue, name, originEnableInfo, enableInfo);
                toRemove.remove(name);
            }
        }

        private void checkAdd(
            List<Text> listToAdd,
            Function<String, Boolean> defValue,
            String name,
            @Nullable Map<String, Boolean> originEnableInfo,
            Map<String, Boolean> enableInfo
        ) {
            final Boolean value = enableInfo.get(name);
            listToAdd.add(
                format(
                    name,
                    Text.of(name),
                    value,
                    defValue.apply(name),
                    originEnableInfo != null && !Objects.equals(originEnableInfo.get(name), enableInfo.get(name)),
                    new Tuple<>(
                        GenericArguments.none(),
                        (src, args) -> {
                            Boolean next = next(value);
                            if (next == null) {
                                enableInfo.remove(name);
                            } else {
                                enableInfo.put(name, next);
                            }
                            resend();
                            return CommandResult.success();
                        }
                    )
                )
            );
        }
    }
}
