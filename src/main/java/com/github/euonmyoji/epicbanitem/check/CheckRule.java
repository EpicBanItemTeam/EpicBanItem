package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
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
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
@SuppressWarnings("WeakerAccess")
public class CheckRule implements TextRepresentable {

    public static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9-_]+");
    private final String name;
    /**
     * 0-9 higher first
     */
    private int priority;
    private Map<String, Boolean> enableWorlds;
    private Map<String, Boolean> enableTriggers;
    private QueryExpression query;
    private ConfigurationNode queryNode;
    private @Nullable
    UpdateExpression update;
    private @Nullable
    ConfigurationNode updateNode;
    private @Nullable
    ConfigurationNode configurationNode;

    public CheckRule(String ruleName) {
        this(ruleName, getDefaultQueryNode());
    }

    public CheckRule(String ruleName, CheckRule rule) {
        this(ruleName, rule.queryNode, rule.updateNode, rule.priority, rule.enableWorlds, rule.enableTriggers);
    }

    public CheckRule(String ruleName, ConfigurationNode queryNode) {
        this(ruleName, queryNode, getDefaultUpdateNode());
    }

    public CheckRule(String ruleName, ConfigurationNode queryNode, @Nullable ConfigurationNode updateNode) {
        this(ruleName, queryNode, updateNode, 5, new HashMap<>(), new HashMap<>());
    }

    public CheckRule(String ruleName, ConfigurationNode queryNode, @Nullable ConfigurationNode updateNode, int priority, Map<String, Boolean> enableWorlds, Map<String, Boolean> enableTriggers) {
        Preconditions.checkArgument(checkName(ruleName), "Rule name should match \"[a-z0-9-_]+\"");
        this.name = Objects.requireNonNull(ruleName);
        this.queryNode = queryNode.copy();
        this.query = new QueryExpression(queryNode);
        this.updateNode = Objects.isNull(updateNode) ? null : updateNode.copy();
        this.update = Objects.isNull(updateNode) ? null : new UpdateExpression(updateNode);
        Preconditions.checkArgument(priority >= 0 && priority <= 9, "Priority should between 0 and 9");
        this.priority = priority;
        this.enableWorlds = ImmutableMap.copyOf(Objects.requireNonNull(enableWorlds));
        this.enableTriggers = ImmutableMap.copyOf(Objects.requireNonNull(enableTriggers));
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

    public Map<String, Boolean> getEnableTriggers() {
        return enableTriggers;
    }

    public Map<String, Boolean> getEnableWorlds() {
        return enableWorlds;
    }

    public boolean isEnabledWorld(World world) {
        String worldName = world.getName();
        return enableWorlds.getOrDefault(worldName, EpicBanItem.getSettings().isWorldDefaultEnabled(worldName));
    }

    public boolean isEnabledTrigger(String trigger) {
        return enableTriggers.getOrDefault(trigger, EpicBanItem.getSettings().isTriggerDefaultEnabled(trigger));
    }

    public ConfigurationNode getQueryNode() {
        return queryNode;
    }

    @Nullable
    public ConfigurationNode getUpdateNode() {
        return updateNode;
    }

    public void setConfigurationNode(@Nullable ConfigurationNode configurationNode) {
        this.configurationNode = configurationNode;
    }

    private Text getWorldInfo() {
        return getEnableInfo(enableWorlds);
    }

    private Text getTriggerInfo() {
        return getEnableInfo(enableTriggers);
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
                DataContainer view = origin.getFinalViewUnchecked();
                Optional<QueryResult> optionalQueryResult = query.query(DataQuery.of(), view);
                if (optionalQueryResult.isPresent()) {
                    if (update != null) {
                        update.update(optionalQueryResult.get(), view).apply(view);
                        return CheckResult.concat(origin, this, view);
                    } else {
                        return CheckResult.concat(origin, this);
                    }
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

    public boolean tryFixId(String itemId) {
        if (queryNode.getNode("id").isVirtual()) {
            queryNode.getNode("id").setValue(itemId);
            query = new QueryExpression(queryNode);
            configurationNode = null;
            return true;
        }
        return false;
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
            int priority = node.getNode("priority").getInt(5);
            Map<String, Boolean> enableWorld = new HashMap<>();
            if (node.getNode("enabled-worlds").hasListChildren()) {
                node.getNode("enabled-worlds").getList(TypeToken.of(String.class)).forEach(s -> enableWorld.put(s, true));
            } else {
                node.getNode("enabled-worlds").getChildrenMap().forEach((k, v) -> enableWorld.put(k.toString(), v.getBoolean()));
            }
            Map<String, Boolean> enableTriggers = new HashMap<>();
            ConfigurationNode triggerNode = node.getNode("use-trigger");
            triggerNode.getChildrenMap().forEach((k, v) -> enableTriggers.put(k.toString(), v.getBoolean()));
            ConfigurationNode queryNode = node.getNode("query");
            ConfigurationNode updateNode = node.getNode("update");
            if (Objects.isNull(updateNode.getValue()) && node.getNode("remove").getBoolean(false)) {
                updateNode = getDefaultUpdateNode();
            }
            return new CheckRule(name, queryNode, updateNode, priority, enableWorld, enableTriggers);
        }

        @Override
        public void serialize(TypeToken<?> type, CheckRule rule, ConfigurationNode node) {
            if (rule == null) {
                return;
            }
            if (rule.configurationNode != null) {
                node.setValue(rule.configurationNode);
                return;
            }
            node.getNode("name").setValue(rule.name);
            node.getNode("priority").setValue(rule.priority);
            rule.enableWorlds.forEach((k, v) -> node.getNode("enabled-worlds", k).setValue(v));
            rule.enableTriggers.forEach((k, v) -> node.getNode("use-trigger", k).setValue(v));
            node.getNode("query").setValue(rule.queryNode);
            node.getNode("update").setValue(rule.updateNode);
        }
    }

    @SuppressWarnings("UnusedReturnValue for builder")
    public static final class Builder {
        private String name;
        private int priority = 5;
        private Map<String, Boolean> enableWorlds = new TreeMap<>();
        private Map<String, Boolean> enableTriggers = new TreeMap<>();
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
            this.enableWorlds = new TreeMap<>(checkRule.enableWorlds);
            this.enableTriggers = new TreeMap<>(checkRule.enableTriggers);
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

        public Builder enableWorlds(Map<String, Boolean> enableWorlds) {
            this.enableWorlds = new TreeMap<>(enableWorlds);
            return this;
        }

        public Builder enableTriggers(Map<String, Boolean> enableTriggers) {
            this.enableTriggers = new TreeMap<>(enableWorlds);
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

        public Map<String, Boolean> getEnableWorlds() {
            return enableWorlds;
        }

        public Map<String, Boolean> getEnableTriggers() {
            return enableTriggers;
        }

        public ConfigurationNode getQueryNode() {
            return queryNode;
        }

        @Nullable
        public ConfigurationNode getUpdateNode() {
            return updateNode;
        }

        public CheckRule build() {
            return new CheckRule(name, queryNode, updateNode, priority, enableWorlds, enableTriggers);
        }
    }
}
