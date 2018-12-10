package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
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
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

@NonnullByDefault
public class CommandEditor extends AbstractCommand {
    private static Map<UUID, Editor> editorMap = new HashMap<>();

    public CommandEditor() {
        super("editor");
    }

    public static void add(Player player, String name, boolean sendMessage) {
        Editor editor = new Editor(player.getUniqueId(), name);
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

    private static Tristate toTristate(@Nullable Boolean b) {
        if (b == null) {
            return Tristate.UNDEFINED;
        } else {
            return Tristate.fromBoolean(b);
        }
    }

    private static String toString(@Nullable Boolean b) {
        return toTristate(b).toString();
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
        if (editor == null) {// TODO: 2018/11/25 CommandCreate
            src.sendMessage(getMessage("useOtherFirst"));
        } else {
            editor.resend();
        }
        return CommandResult.success();
    }

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
         * Triggers :{triggers}
         * Worlds   :{worlds}
         * QueryExpression : not editable
         * UpdateExpression: not editable
         * <p>
         * Save
         */
        private Text genText() {
            Text.Builder builder = Text.builder();
            //Header
            builder.append(getMessage("header"), Text.NEW_LINE);
            //Name
            builder.append(getMessage("name", "name",
                    format(
                            ruleBuilder.getName(),
                            origin != null && !origin.getName().equals(ruleBuilder.getName()),
                            new Tuple<>(
                                    EpicBanItemArgs.patternString(Text.of("name"), CheckRule.NAME_PATTERN),
                                    (src, args) -> {
                                        ruleBuilder.name(args.<String>getOne("name").get());
                                        resend();
                                        return CommandResult.success();
                                    }
                            )
                    ))).append(Text.NEW_LINE);
            //Priority
            builder.append(getMessage("priority", "priority",
                    format(
                            ruleBuilder.getPriority(),
                            origin != null && origin.getPriority() != ruleBuilder.getPriority(),
                            new Tuple<>(
                                    GenericArguments.integer(Text.of("priority")),
                                    (src, args) -> {
                                        int priority = args.<Integer>getOne("priority").get();
                                        if (priority < 0 || priority > 9) {
                                            // TODO: 2018/11/24
                                            throw new CommandException(Text.EMPTY);
                                        }
                                        ruleBuilder.priority(priority);
                                        resend();
                                        return CommandResult.success();
                                    }
                            )
                    ))).append(Text.NEW_LINE);
            //Triggers
            Set<String> setTriggers = new HashSet<>(ruleBuilder.getEnableTriggers().keySet());
            List<Text> triggers = new ArrayList<>();
            for (String trigger : Triggers.getDefaultTriggers()) {
                final Boolean value = ruleBuilder.getEnableTriggers().get(trigger);
                triggers.add(format(
                        trigger,
                        EpicBanItem.getMessages().getMessage("epicbanitem.triggers." + trigger),
                        value,
                        EpicBanItem.getSettings().isTriggerDefaultEnabled(trigger),
                        origin != null && !Objects.equals(origin.getEnableTriggers().get(trigger), ruleBuilder.getEnableTriggers().get(trigger)),
                        new Tuple<>(
                                GenericArguments.none(),
                                (src, args) -> {
                                    Boolean next = next(value);
                                    if (next == null) {
                                        ruleBuilder.getEnableTriggers().remove(trigger);
                                    } else {
                                        ruleBuilder.getEnableTriggers().put(trigger, next);
                                    }
                                    resend();
                                    return CommandResult.success();
                                }
                        )
                ));
                setTriggers.remove(trigger);
            }
            //triggers that not in the default set?
            for (String trigger : setTriggers) {
                final Boolean value = ruleBuilder.getEnableTriggers().get(trigger);
                triggers.add(format(
                        trigger,
                        EpicBanItem.getMessages().getMessage("epicbanitem.triggers." + trigger),
                        value,
                        EpicBanItem.getSettings().isTriggerDefaultEnabled(trigger),
                        origin != null && !Objects.equals(origin.getEnableTriggers().get(trigger), ruleBuilder.getEnableTriggers().get(trigger)),
                        new Tuple<>(
                                GenericArguments.none(),
                                (src, args) -> {
                                    Boolean next = next(value);
                                    if (next == null) {
                                        ruleBuilder.getEnableTriggers().remove(trigger);
                                    } else {
                                        ruleBuilder.getEnableTriggers().put(trigger, next);
                                    }
                                    resend();
                                    return CommandResult.success();
                                }
                        )
                ));
            }
            builder.append(getMessage("triggers", "triggers",
                    TextUtil.join(Text.builder("  ").style(TextStyles.RESET).build(), triggers)))
                    .append(Text.NEW_LINE);
            //Worlds
            Set<String> setWorlds = new HashSet<>(ruleBuilder.getEnableWorlds().keySet());
            List<Text> worlds = new ArrayList<>();
            for (WorldProperties worldProperties : Sponge.getServer().getAllWorldProperties()) {
                String worldName = worldProperties.getWorldName();
                final Boolean value = ruleBuilder.getEnableWorlds().get(worldName);
                worlds.add(format(
                        worldName,
                        Text.of(worldName),
                        value,
                        EpicBanItem.getSettings().isWorldDefaultEnabled(worldName),
                        origin != null && !Objects.equals(origin.getEnableWorlds().get(worldName), ruleBuilder.getEnableWorlds().get(worldName)),
                        new Tuple<>(
                                GenericArguments.none(),
                                (src, args) -> {
                                    Boolean next = next(value);
                                    if (next == null) {
                                        ruleBuilder.getEnableWorlds().remove(worldName);
                                    } else {
                                        ruleBuilder.getEnableWorlds().put(worldName, next);
                                    }
                                    resend();
                                    return CommandResult.success();
                                }
                        )
                ));
                setWorlds.remove(worldName);
            }
            //worlds that not in the sponge data?
            for (String worldName : setWorlds) {
                final Boolean value = ruleBuilder.getEnableWorlds().get(worldName);
                worlds.add(format(
                        worldName,
                        Text.of(worldName),
                        value,
                        EpicBanItem.getSettings().isWorldDefaultEnabled(worldName),
                        origin != null && !Objects.equals(origin.getEnableWorlds().get(worldName), ruleBuilder.getEnableWorlds().get(worldName)),
                        new Tuple<>(
                                GenericArguments.none(),
                                (src, args) -> {
                                    Boolean next = next(value);
                                    if (next == null) {
                                        ruleBuilder.getEnableWorlds().remove(worldName);
                                    } else {
                                        ruleBuilder.getEnableWorlds().put(worldName, next);
                                    }
                                    resend();
                                    return CommandResult.success();
                                }
                        )
                ));
            }
            builder.append(getMessage("worlds", "worlds",
                    TextUtil.join(Text.builder("  ").style(TextStyles.RESET).build(), worlds))).append(Text.NEW_LINE);
            //Query
            String queryKey = CommandCallback.add(owner,
                    GenericArguments.remainingRawJoinedStrings(Text.of("query-rule")),
                    (src, args) -> {
                        String rule = args.<String>getOne("query-rule").get();
                        try {
                            ConfigurationNode node = TextUtil.serializeStringToConfigNode(rule);
                            ruleBuilder.queryNode(node);
                        } catch (IOException e) {
                            throw new CommandException(Text.of("Error"), e);
                        }
                        resend();
                        return CommandResult.success();
                    });
            List<Text> queries = new ArrayList<>();
            queries.add(formatNode(
                    getMessage("custom").toPlain(),
                    ruleBuilder.getQueryNode(),
                    queryKey
            ));
            queries.add(formatNode(
                    getMessage("default").toPlain(),
                    CheckRule.getDefaultQueryNode(),
                    queryKey
            ));
            // TODO: 2018/11/24 History of query command
            if (origin != null) {
                queries.add(formatNode(
                        getMessage("origin").toPlain(),
                        origin.getQueryNode(),
                        queryKey
                ));
            }
            builder.append(getMessage("query", "options", TextUtil.join(Text.builder("  ").style(TextStyles.RESET).build(), queries))).append(Text.NEW_LINE);
            //updates
            String updateKey = CommandCallback.add(owner,
                    GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of("update-rule"))),
                    (src, args) -> {
                        Optional<String> rule = args.getOne("update-rule");
                        if (rule.isPresent()) {
                            try {
                                ConfigurationNode node = TextUtil.serializeStringToConfigNode(rule.get());
                                ruleBuilder.updateNode(node);
                            } catch (IOException e) {
                                throw new CommandException(Text.of("Error"), e);
                            }
                        } else {
                            ruleBuilder.updateNode(null);
                        }
                        resend();
                        return CommandResult.success();
                    });
            List<Text> updates = new ArrayList<>();
            updates.add(formatNode(
                    getMessage("custom").toPlain(),
                    ruleBuilder.getUpdateNode(),
                    updateKey
            ));
            updates.add(formatNode(
                    getMessage("default").toPlain(),
                    CheckRule.getDefaultUpdateNode(),
                    updateKey
            ));
            // TODO: 2018/11/24 History of update command
            if (origin != null) {
                updates.add(formatNode(
                        getMessage("origin").toPlain(),
                        origin.getUpdateNode(),
                        updateKey
                ));
            }
            builder.append(getMessage("update", "options", TextUtil.join(Text.builder("  ").style(TextStyles.RESET).build(), updates))).append(Text.NEW_LINE);
            //Save
            builder.append(
                    getMessage("save").toBuilder().style(TextStyles.UNDERLINE, TextStyles.BOLD).color(TextColors.RED)
                            .onClick(TextActions.runCommand(String.format("/%s cb %s", EpicBanItem.getMainCommandAlias(),
                                    CommandCallback.add(owner, GenericArguments.none(), (src, args) -> {
                                        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
                                        CheckRule rule = ruleBuilder.build();
                                        // remove th origin one.
                                        if (origin != null) {
                                            // TODO: 2018/11/24 Is the rule with that name the rule we get on created the editor.
                                            // TODO: 2018/12/10 Should we consider completable future?
                                            service.removeRule(origin);
                                        }
                                        // TODO: 2018/11/25 Warn on no id matches ?
                                        Optional<String> id = Optional.ofNullable(ruleBuilder.getQueryNode().getNode("id").getString());
                                        service.appendRule(rule).thenRun(() -> src.sendMessage(getMessage("saved")));
                                        editorMap.remove(owner);
                                        return CommandResult.success();
                                    })
                            ))).toText()
            );
            return builder.toText();
        }

        private Text format(Object value, boolean edited, Tuple<CommandElement, CommandExecutor> action) {
            String ebi = EpicBanItem.getMainCommandAlias();
            Text.Builder builder = Text.builder(value.toString());
            //Mark edited parts bold.
            builder.style(builder.getStyle().bold(edited));
            builder.color(TextColors.BLUE);
            if (action.getFirst().equals(GenericArguments.none())) {
                builder.onClick(TextActions.runCommand(String.format("/%s cb %s", ebi, CommandCallback.add(owner, action))));
            } else {
                builder.onClick(TextActions.suggestCommand(String.format("/%s cb %s %s", ebi, CommandCallback.add(owner, action), value)));
            }
            builder.onHover(TextActions.showText(getMessage("click")));
            return builder.build();
        }

        //
        private Text format(String text, Text hover, @Nullable Boolean vale, boolean defaultVale, boolean edited, Tuple<CommandElement, CommandExecutor> action) {
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
            Text display = getMessage("tristate.display",
                    "value", getMessage("tristate." + CommandEditor.toString(vale).toLowerCase()),
                    "default", getMessage("tristate." + CommandEditor.toString(defaultVale).toLowerCase())
            );
            Text click = getMessage("tristate.click",
                    "to", getMessage("tristate." + CommandEditor.toString(next(vale)).toLowerCase())
            );
            if (!hover.isEmpty()) {
                tri.append(hover, Text.NEW_LINE, Text.NEW_LINE);
            }
            tri.append(display, Text.NEW_LINE).append(click);
            builder.onHover(TextActions.showText(tri.build()));
            return builder.build();
        }

        private Text formatNode(String display, @Nullable ConfigurationNode node, String key) {
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
            Text hover = Text.of(nodeString, Text.NEW_LINE, getMessage("click"));
            builder.onHover(TextActions.showText(hover));
            builder.onClick(TextActions.suggestCommand(String.format("/%s cb %s %s", ebi, key, suggestString)));
            return builder.toText();
        }
    }
}
