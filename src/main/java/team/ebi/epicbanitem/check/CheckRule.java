package team.ebi.epicbanitem.check;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.slf4j.Logger;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.CheckResult;
import team.ebi.epicbanitem.api.CheckRuleLocation;
import team.ebi.epicbanitem.api.CheckRuleTrigger;
import team.ebi.epicbanitem.command.CommandCheck;
import team.ebi.epicbanitem.util.TextUtil;
import team.ebi.epicbanitem.util.nbt.QueryExpression;
import team.ebi.epicbanitem.util.nbt.QueryResult;
import team.ebi.epicbanitem.util.nbt.UpdateExpression;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * @author The EpicBanItem Team
 */
@NonnullByDefault
@SuppressWarnings({ "WeakerAccess" })
public class CheckRule implements TextRepresentable {

    public static Builder builder() {
        return new CheckRule.Builder();
    }

    @Deprecated
    public static Builder builder(String name) {
        return new Builder().name(CheckRuleLocation.of(name));
    }

    public static Builder builder(CheckRule checkRule) {
        return new Builder(checkRule);
    }

    /**
     * the name which should match {@link CheckRuleLocation#NAME_PATTERN}
     */
    private final CheckRuleLocation name;
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
    private final ImmutableMap<CheckRuleTrigger, Boolean> triggerSettings;
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

    /**
     * setting the info sent to player when this rule affect.
     */
    @Nullable
    private final String customMessageString;

    private CheckRule(
        CheckRuleLocation ruleName,
        ConfigurationNode queryNode,
        @Nullable ConfigurationNode updateNode,
        String legacyName,
        int priority,
        Tristate worldDefaultSetting,
        Map<String, Boolean> worldSettings,
        Tristate triggerDefaultSetting,
        Map<CheckRuleTrigger, Boolean> triggerSettings,
        @Nullable String customMessageString
    ) {
        this.worldDefaultSetting = worldDefaultSetting;
        this.triggerDefaultSetting = triggerDefaultSetting;
        this.name = ruleName;
        this.legacyName = legacyName;
        this.queryNode = queryNode.copy();
        this.query = new QueryExpression(queryNode);
        this.updateNode = Objects.isNull(updateNode) ? null : updateNode.copy();
        this.update = Objects.isNull(updateNode) ? null : new UpdateExpression(updateNode);
        Preconditions.checkArgument(priority >= 0 && priority <= 9, "Priority should between 0 and 9");
        this.priority = priority;
        this.worldSettings = ImmutableMap.copyOf(worldSettings);
        this.triggerSettings = ImmutableMap.copyOf(triggerSettings);
        this.customMessageString = customMessageString;
    }

    public static Comparator<CheckRule> getDefaultComparator() {
        return Comparator.comparing(CheckRule::getPriority).thenComparing(CheckRule::getName);
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

    public CheckRuleLocation getName() {
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

    public Map<CheckRuleTrigger, Boolean> getTriggerSettings() {
        return triggerSettings;
    }

    public Optional<String> getCustomMessageString() {
        return Optional.ofNullable(customMessageString);
    }

    public boolean isEnabledWorld(World world) {
        String worldName = world.getName();
        if (worldDefaultSetting == Tristate.UNDEFINED) {
            return worldSettings.getOrDefault(worldName, EpicBanItem.getSettings().isWorldDefaultEnabled(worldName));
        } else {
            return worldSettings.getOrDefault(worldName, worldDefaultSetting.asBoolean());
        }
    }

    public boolean isEnabledTrigger(CheckRuleTrigger trigger) {
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

    private Text getEnableInfo(Map<?, Boolean> enableMap) {
        Text.Builder builder = Text.builder("[");
        if (!enableMap.isEmpty()) {
            Text separator = Text.of();
            for (Map.Entry<?, Boolean> entry : enableMap.entrySet()) {
                builder.append(separator).append(Text.of(entry.getValue() ? "+" : "-")).append(Text.of(entry.getKey()));
                separator = Text.of(", ");
            }
        }
        return builder.append(Text.of("]")).build();
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
    public CheckResult check(CheckResult origin, World world, CheckRuleTrigger trigger, @Nullable Subject subject) {
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
                origin = origin.banFor(predicate);
                if (queryResult[0] != null && origin.isBanned()) {
                    CheckResult.Banned newOne = (CheckResult.Banned) origin;
                    if (this.customMessageString == null) {
                        newOne = newOne.withMessage(this.toText());
                    } else {
                        newOne = newOne.withMessage(this.toText(), this.customMessageString);
                    }
                    if (update != null) {
                        newOne =
                            newOne.updateBy(
                                view -> {
                                    update.update(queryResult[0], view).apply(view);
                                    return view;
                                }
                            );
                    }
                    return newOne;
                }
                return origin;
            }
        }
        return origin;
    }

    public boolean hasBypassPermission(Subject subject, CheckRuleTrigger trigger) {
        Context newContext = new Context("epicbanitem-trigger", trigger.getId());
        return subject.hasPermission(Sets.union(subject.getActiveContexts(), Collections.singleton(newContext)), "epicbanitem.bypass." + name);
    }

    @Override
    public Text toText() {
        //TODO: custom display name?
        return Text.builder(getName().toString()).build();
    }

    @Singleton
    public static class BuilderSerializer implements TypeSerializer<Builder> {
        @Inject
        private Logger logger;

        @Override
        public Builder deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            String legacyName = node.getNode("legacy-name").getString("");
            int priority = node.getNode("priority").getInt(5);
            Map<String, Boolean> enableWorld = new HashMap<>();
            node.getNode("enabled-worlds").getChildrenMap().forEach((k, v) -> enableWorld.put(k.toString(), v.getBoolean()));
            Tristate worldDefaultSetting = node.getNode("world-default-setting").getValue(TypeToken.of(Tristate.class), Tristate.UNDEFINED);
            Map<CheckRuleTrigger, Boolean> enableTriggers = new HashMap<>();
            ConfigurationNode triggerNode = node.getNode("use-trigger");
            triggerNode
                .getChildrenMap()
                .forEach(
                    (k, v) -> {
                        Optional<CheckRuleTrigger> optionalTrigger = Sponge.getRegistry().getType(CheckRuleTrigger.class, k.toString());
                        if (!optionalTrigger.isPresent()) {
                            logger.warn("Find unknown trigger {} at check rule {}, it will be ignored.", k.toString(), String.join(".", Arrays.stream(node.getPath()).map(String::valueOf).toString()));
                        } else {
                            enableTriggers.put(optionalTrigger.get(), v.getBoolean());
                        }
                    }
                );
            Tristate triggerDefaultSetting = node.getNode("trigger-default-setting").getValue(TypeToken.of(Tristate.class), Tristate.UNDEFINED);
            ConfigurationNode queryNode = node.getNode("query");
            ConfigurationNode updateNode = node.getNode("update");
            String customMessageString = node.getNode("custom-message").getString();
            return builder()
                .legacyName(legacyName)
                .queryNode(queryNode)
                .updateNode(updateNode.isVirtual() ? null : updateNode)
                .priority(priority)
                .worldDefaultSetting(worldDefaultSetting)
                .enableWorlds(enableWorld)
                .triggerDefaultSetting(triggerDefaultSetting)
                .enableTriggers(enableTriggers)
                .customMessage(customMessageString);
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public void serialize(TypeToken<?> type, @Nullable Builder builder, ConfigurationNode node) throws ObjectMappingException {
            Objects.requireNonNull(builder);
            if (!builder.legacyName.isEmpty()) {
                node.getNode("legacy-name").setValue(builder.legacyName);
            }
            node.getNode("priority").setValue(builder.priority);
            node.getNode("world-default-setting").setValue(TypeToken.of(Tristate.class), builder.worldDefaultSetting);
            builder.worldSettings.forEach((k, v) -> node.getNode("enabled-worlds", k).setValue(v));
            node.getNode("trigger-default-setting").setValue(TypeToken.of(Tristate.class), builder.triggerDefaultSetting);
            builder.triggerSettings.forEach((k, v) -> node.getNode("use-trigger", k.getId()).setValue(v));
            node.getNode("query").setValue(builder.queryNode);
            node.getNode("update").setValue(builder.updateNode);
            node.getNode("custom-message").setValue(builder.customMessageString);
        }
    }

    @SuppressWarnings("UnusedReturnValue for builder")
    public static final class Builder {
        @Nullable
        private CheckRuleLocation name = null;
        private String legacyName = "";
        private int priority = 5;
        private Tristate worldDefaultSetting = Tristate.UNDEFINED;
        private Map<String, Boolean> worldSettings = new TreeMap<>();
        private Tristate triggerDefaultSetting = Tristate.UNDEFINED;
        private Map<CheckRuleTrigger, Boolean> triggerSettings = new TreeMap<>(Comparator.comparing(CheckRuleTrigger::getId));
        private ConfigurationNode queryNode;

        @Nullable
        private ConfigurationNode updateNode;

        @Nullable
        private String customMessageString;

        private Builder() {
            queryNode = getDefaultQueryNode();
        }

        private Builder(CheckRule checkRule) {
            this.name = checkRule.name;
            this.legacyName = checkRule.legacyName;
            this.priority = checkRule.priority;
            this.worldSettings = new TreeMap<>(checkRule.worldSettings);
            this.worldDefaultSetting = checkRule.worldDefaultSetting;
            this.triggerSettings = new TreeMap<>(Comparator.comparing(CatalogType::getId));
            this.triggerSettings.putAll(checkRule.triggerSettings);
            this.triggerDefaultSetting = checkRule.triggerDefaultSetting;
            this.queryNode = checkRule.queryNode;
            this.updateNode = checkRule.updateNode;
            this.customMessageString = checkRule.customMessageString;
        }

        public Builder name(CheckRuleLocation name) {
            this.name = name;
            return this;
        }

        public Builder legacyName(String legacyName) {
            this.legacyName = legacyName;
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

        public Builder enableTriggers(Map<CheckRuleTrigger, Boolean> enableTriggers) {
            this.triggerSettings = new TreeMap<>(Comparator.comparing(CatalogType::getId));
            this.triggerSettings.putAll(enableTriggers);
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

        public Builder customMessage(@Nullable String customMessage) {
            this.customMessageString = customMessage;
            return this;
        }

        public Optional<CheckRuleLocation> getName() {
            return Optional.ofNullable(name);
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

        public Map<CheckRuleTrigger, Boolean> getTriggerSettings() {
            return triggerSettings;
        }

        public ConfigurationNode getQueryNode() {
            return queryNode;
        }

        @Nullable
        public ConfigurationNode getUpdateNode() {
            return updateNode;
        }

        @Nullable
        public String getCustomMessageString() {
            return customMessageString;
        }

        public CheckRule build() {
            return new CheckRule(
                Preconditions.checkNotNull(name, "name should not be null"),
                queryNode,
                updateNode,
                legacyName,
                priority,
                worldDefaultSetting,
                worldSettings,
                triggerDefaultSetting,
                triggerSettings,
                customMessageString
            );
        }
    }
}
