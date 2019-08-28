package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.api.CheckResult;
import com.github.euonmyoji.epicbanitem.command.CommandCheck;
import com.github.euonmyoji.epicbanitem.message.Messages;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryResult;
import com.github.euonmyoji.epicbanitem.util.nbt.UpdateExpression;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
@SuppressWarnings({"WeakerAccess", "NullableProblems"})
public class CheckRule implements TextRepresentable {

    public static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9-_]+");
    /**
     * the name which should match {@link #NAME_PATTERN}
     */
    private final String name;
    /**
     * the legacy name
     */
    private final String legacyName;
    /**
     * priority (0-9), higher first
     */
    private final int priority;
    /**
     * world default setting, {@link EpicBanItem#getSettings()} will be used if it is {@link Tristate#UNDEFINED}
     */
    private final Tristate worldDefaultSetting;
    /**
     * world settings, {@link #worldDefaultSetting} will be used if the corresponding value does not exist
     */
    private final ImmutableMap<String, Boolean> worldSettings;
    /**
     * trigger default setting, {@link EpicBanItem#getSettings()} will be used if it is {@link Tristate#UNDEFINED}
     */
    private final Tristate triggerDefaultSetting;
    /**
     * trigger settings, {@link #triggerDefaultSetting} will be used if the corresponding value does not exist
     */
    private final ImmutableMap<String, Boolean> triggerSettings;
    /**
     * query expression
     */
    private final QueryExpression query;
    /**
     * serialized query expression
     */
    private final ConfigurationNode queryNode;
    /**
     * update expression
     */
    @Nullable
    private final UpdateExpression update;
    /**
     * serialized update expression
     */
    @Nullable
    private final ConfigurationNode updateNode;

    public CheckRule(String ruleName) {
        this(ruleName, getDefaultQueryNode());
    }

    public CheckRule(String ruleName, CheckRule rule) {
        this(ruleName, rule.queryNode, rule.updateNode, rule.legacyName, rule.priority, rule.worldDefaultSetting, rule.worldSettings, rule.triggerDefaultSetting, rule.triggerSettings);
    }

    public CheckRule(String ruleName, ConfigurationNode queryNode) {
        this(ruleName, queryNode, getDefaultUpdateNode());
    }

    public CheckRule(String ruleName, ConfigurationNode queryNode, @Nullable ConfigurationNode updateNode) {
        this(ruleName, queryNode, updateNode, "", 5, Tristate.UNDEFINED, Collections.emptyMap(), Tristate.UNDEFINED, Collections.emptyMap());
    }

    public CheckRule(String ruleName, ConfigurationNode queryNode, @Nullable ConfigurationNode updateNode, String legacyName, int priority, Tristate worldDefaultSetting, Map<String, Boolean> worldSettings, Tristate triggerDefaultSetting, Map<String, Boolean> triggerSettings) {
        this.worldDefaultSetting = worldDefaultSetting;
        this.triggerDefaultSetting = triggerDefaultSetting;
        Preconditions.checkArgument(checkName(ruleName), "Rule name should match \"[a-z0-9-_]+\"");
        this.name = Objects.requireNonNull(ruleName);
        this.legacyName = legacyName;
        this.queryNode = queryNode.copy();
        this.query = new QueryExpression(queryNode);
        this.updateNode = Objects.isNull(updateNode) ? null : updateNode.copy();
        this.update = Objects.isNull(updateNode) ? null : new UpdateExpression(updateNode);
        Preconditions.checkArgument(priority >= 0 && priority <= 9, "Priority should between 0 and 9");
        this.priority = priority;
        this.worldSettings = ImmutableMap.copyOf(Objects.requireNonNull(worldSettings));
        this.triggerSettings = ImmutableMap.copyOf(Objects.requireNonNull(triggerSettings));
    }

    public static boolean checkName(@Nullable String s) {
        return s != null && NAME_PATTERN.matcher(s).matches();
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static Builder builder(CheckRule checkRule) {
        return new Builder(checkRule);
    }

    public static ConfigurationNode getDefaultQueryNode() {
        try {
            return TextUtil.serializeStringToConfigNode("{}");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ConfigurationNode getDefaultUpdateNode() {
        try {
            return TextUtil.serializeStringToConfigNode("{\"$set\": {id: \"minecraft:air\", Damage: 0}}");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public Tristate getWorldDefaultSetting() {
        return worldDefaultSetting;
    }

    public Map<String, Boolean> getWorldSettings() {
        return worldSettings;
    }

    public Tristate getTriggerDefaultSetting() {
        return triggerDefaultSetting;
    }

    public Map<String, Boolean> getTriggerSettings() {
        return triggerSettings;
    }

    public boolean isEnabledWorld(World world) {
        String worldName = world.getName();
        if (worldDefaultSetting == Tristate.UNDEFINED) {
            return worldSettings.getOrDefault(worldName, EpicBanItem.getSettings().isWorldDefaultEnabled(worldName));
        } else {
            return worldSettings.getOrDefault(worldName, worldDefaultSetting.asBoolean());
        }
    }

    public boolean isEnabledTrigger(String trigger) {
        if (triggerDefaultSetting == Tristate.UNDEFINED) {
            return triggerSettings.getOrDefault(trigger, EpicBanItem.getSettings().isTriggerDefaultEnabled(trigger));
        } else {
            return triggerSettings.getOrDefault(trigger, triggerDefaultSetting.asBoolean());
        }
    }

    public ConfigurationNode getQueryNode() {
        return queryNode;
    }

    @Nullable
    public ConfigurationNode getUpdateNode() {
        return updateNode;
    }

    private Text getWorldInfo() {
        return Text.of(worldDefaultSetting.toString().toLowerCase(), getEnableInfo(worldSettings));
    }

    private Text getTriggerInfo() {
        return Text.of(worldDefaultSetting.toString().toLowerCase(), getEnableInfo(triggerSettings));
    }

    private Text getEnableInfo(Map<String, Boolean> enableMap) {
        Text.Builder builder = Text.builder("[");
        if (!enableMap.isEmpty()) {
            Text separator = Text.of();
            for (Map.Entry<String, Boolean> entry : enableMap.entrySet()) {
                builder.append(separator).append(Text.of(entry.getValue() ? "+" : "-")).append(Text.of(entry.getKey()));
                separator = Text.of(", ");
            }
        }
        return builder.append(Text.of("]")).build();
    }

    private Text getQueryInfo() {
        try {
            return Text.of(TextUtil.deserializeConfigNodeToString(queryNode));
        } catch (IOException e) {
            EpicBanItem.getLogger().error("Failed to deserialize ConfigNode to String", e);
            return EpicBanItem.getMessages().getMessage("epicbanitem.error.failDeserialize");
        }
    }

    private Text getUpdateInfo() {
        if (updateNode == null) {
            return Text.of("No Update");
        }
        try {
            return Text.of(TextUtil.deserializeConfigNodeToString(updateNode));
        } catch (IOException e) {
            EpicBanItem.getLogger().error("Failed to deserialize ConfigNode to String", e);
            return EpicBanItem.getMessages().getMessage("epicbanitem.error.failDeserialize");
        }
    }

    public Predicate<String> idIndexFilter() {
        return id -> !Tristate.FALSE.equals(query.filterString(DataQuery.of("id"), id));
    }

    /**
     * @param origin  原检查结果
     * @param world   检查发生世界名
     * @param trigger 检查发生trigger
     * @param subject 被检查的权限主体
     * @return 检查结果
     */
    public CheckResult check(CheckResult origin, World world, String trigger, @Nullable Subject subject) {
        if (isEnabledTrigger(trigger) && isEnabledWorld(world)) {
            if (subject == null || !hasBypassPermission(subject, trigger)) {
                QueryResult[] queryResult = new QueryResult[1];
                Predicate<DataView> predicate = view -> {
                    Optional<QueryResult> optionalQueryResult = this.query.query(DataQuery.of(), view);
                    if (optionalQueryResult.isPresent()) {
                        queryResult[0] = optionalQueryResult.get();
                        CommandCheck.addContext(this);
                        return true;
                    }
                    return false;
                };
                if (update != null) {
                    return origin.banFor(predicate, this, view -> {
                        update.update(queryResult[0], view).apply(view);
                        return view;
                    });
                } else {
                    return origin.banFor(predicate, this);
                }
            }
        }
        return origin;
    }

    @Override
    public Text toText() {
        Messages messages = EpicBanItem.getMessages();
        Text.Builder builder = Text.builder();
        builder.append(Text.of(this.getName()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.worlds", "worlds", this.getWorldInfo()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.triggers", "triggers", this.getTriggerInfo()), Text.NEW_LINE);
        return Text.builder(getName()).onHover(TextActions.showText(builder.build())).build();
    }

    public Text info() {
        // TODO: 点击补全指令?
        Messages messages = EpicBanItem.getMessages();
        Text.Builder builder = Text.builder();
        builder.append(Text.of(this.getName()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.worlds", "worlds", this.getWorldInfo()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.triggers", "triggers", this.getTriggerInfo()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.query", "query", this.getQueryInfo()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.update", "update", this.getUpdateInfo()), Text.NEW_LINE);
        return builder.build();
    }

    private boolean hasBypassPermission(Subject subject, String trigger) {
        return subject.hasPermission(getContext(subject, trigger), "epicbanitem.bypass." + name);
    }

    private Set<Context> getContext(Subject subject, String trigger) {
        Context newContext = new Context("epicbanitem-trigger", trigger);
        return Sets.union(subject.getActiveContexts(), Collections.singleton(newContext));
    }

    public static class Serializer implements TypeSerializer<CheckRule> {

        @Override
        public CheckRule deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            String name = Objects.requireNonNull(node.getNode("name").getString());
            String legacyName = node.getNode("legacy-name").getString("");
            int priority = node.getNode("priority").getInt(5);
            Map<String, Boolean> enableWorld = new HashMap<>();
            Tristate worldDefaultSetting = Tristate.UNDEFINED;
            if (node.getNode("enabled-worlds").hasListChildren()) {
                node.getNode("enabled-worlds").getList(TypeToken.of(String.class)).forEach(s -> enableWorld.put(s, true));
                worldDefaultSetting = Tristate.FALSE;
            } else {
                node.getNode("enabled-worlds").getChildrenMap().forEach((k, v) -> enableWorld.put(k.toString(), v.getBoolean()));
                if (!node.getNode("world-default-setting").isVirtual()) {
                    worldDefaultSetting = Tristate.fromBoolean(node.getNode("world-default-setting").getBoolean());
                }
            }
            Map<String, Boolean> enableTriggers = new HashMap<>();
            ConfigurationNode triggerNode = node.getNode("use-trigger");
            triggerNode.getChildrenMap().forEach((k, v) -> enableTriggers.put(k.toString(), v.getBoolean()));
            Tristate triggerDefaultSetting;
            if (!node.getNode("trigger-default-setting").isVirtual()) {
                triggerDefaultSetting = Tristate.fromBoolean(node.getNode("trigger-default-setting").getBoolean());
            } else {
                triggerDefaultSetting = Tristate.UNDEFINED;
            }
            ConfigurationNode queryNode = node.getNode("query");
            ConfigurationNode updateNode = node.getNode("update");
            if (updateNode.isVirtual() && node.getNode("remove").getBoolean(false)) {
                updateNode = getDefaultUpdateNode();
            }
            return new CheckRule(name, queryNode, updateNode.isVirtual() ? null : updateNode, legacyName, priority, worldDefaultSetting, enableWorld, triggerDefaultSetting, enableTriggers);
        }

        @Override
        public void serialize(TypeToken<?> type, @Nullable CheckRule rule, ConfigurationNode node) {
            Objects.requireNonNull(rule);
            node.getNode("name").setValue(rule.name);
            if (!rule.legacyName.isEmpty()) {
                node.getNode("legacy-name").setValue(rule.legacyName);
            }
            node.getNode("priority").setValue(rule.priority);
            BiConsumer<String, Tristate> setTristate = (key, value) -> {
                if (value == Tristate.UNDEFINED) {
                    node.removeChild(key);
                } else {
                    node.getNode(key).setValue(value.asBoolean());
                }
            };
            setTristate.accept("world-default-setting", rule.worldDefaultSetting);
            rule.worldSettings.forEach((k, v) -> node.getNode("enabled-worlds", k).setValue(v));
            setTristate.accept("trigger-default-setting", rule.triggerDefaultSetting);
            rule.triggerSettings.forEach((k, v) -> node.getNode("use-trigger", k).setValue(v));
            node.getNode("query").setValue(rule.queryNode);
            node.getNode("update").setValue(rule.updateNode);
        }
    }

    @SuppressWarnings("UnusedReturnValue for builder")
    public static final class Builder {
        private String name;
        private String legacyName = "";
        private int priority = 5;
        private Tristate worldDefaultSetting = Tristate.UNDEFINED;
        private Map<String, Boolean> worldSettings = new TreeMap<>();
        private Tristate triggerDefaultSetting = Tristate.UNDEFINED;
        private Map<String, Boolean> triggerSettings = new TreeMap<>();
        private ConfigurationNode queryNode;
        private @Nullable
        ConfigurationNode updateNode;

        private Builder(String name) {
            Preconditions.checkArgument(checkName(name), "Rule name should match \"[a-z0-9-_]+\"");
            this.name = name;
            queryNode = getDefaultQueryNode();
        }

        private Builder(CheckRule checkRule) {
            this.name = checkRule.name;
            this.priority = checkRule.priority;
            this.worldSettings = new TreeMap<>(checkRule.worldSettings);
            this.triggerSettings = new TreeMap<>(checkRule.triggerSettings);
            this.queryNode = checkRule.queryNode;
            this.updateNode = checkRule.updateNode;
        }

        public Builder name(String name) {
            Preconditions.checkArgument(checkName(name), "Rule name should match \"[a-z0-9-_]+\"");
            this.name = name;
            return this;
        }

        public Builder priority(int priority) {
            Preconditions.checkArgument(priority >= 0 && priority <= 9, "Priority should between 0 and 9.");
            this.priority = priority;
            return this;
        }

        public Builder worldDefaultSetting(Tristate tristate) {
            worldDefaultSetting = tristate;
            return this;
        }

        public Builder enableWorlds(Map<String, Boolean> enableWorlds) {
            this.worldSettings = new TreeMap<>(enableWorlds);
            return this;
        }

        public Builder triggerDefaultSetting(Tristate tristate) {
            triggerDefaultSetting = tristate;
            return this;
        }

        public Builder enableTriggers(Map<String, Boolean> enableTriggers) {
            this.triggerSettings = new TreeMap<>(worldSettings);
            return this;
        }

        public Builder queryNode(ConfigurationNode queryNode) {
            this.queryNode = queryNode;
            return this;
        }

        public Builder updateNode(@Nullable ConfigurationNode updateNode) {
            this.updateNode = updateNode;
            return this;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

        public Tristate getWorldDefaultSetting() {
            return worldDefaultSetting;
        }

        public Map<String, Boolean> getWorldSettings() {
            return worldSettings;
        }

        public Tristate getTriggerDefaultSetting() {
            return triggerDefaultSetting;
        }

        public Map<String, Boolean> getTriggerSettings() {
            return triggerSettings;
        }

        public ConfigurationNode getQueryNode() {
            return queryNode;
        }

        @Nullable
        public ConfigurationNode getUpdateNode() {
            return updateNode;
        }

        public CheckRule build() {
            return new CheckRule(name, queryNode, updateNode, legacyName, priority, worldDefaultSetting, worldSettings, triggerDefaultSetting, triggerSettings);
        }
    }
}
