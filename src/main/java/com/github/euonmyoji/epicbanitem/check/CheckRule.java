package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.message.Messages;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryResult;
import com.github.euonmyoji.epicbanitem.util.nbt.UpdateExpression;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

/**
 * @author GiNYAi yinyangshi
 */
@NonnullByDefault
@SuppressWarnings("WeakerAccess")
public class CheckRule {
    // TODO: editable
//    public static Builder builder(){
//        return new Builder();
//    }
//
//    public static class Builder {
//
//        private String name;
//        private ItemType itemType;
//        private BanConfig source;
//        private int priority;
//        private Set<String> enableWorlds = new HashSet<>();
//        private String ignorePermission;
//        private Set<String> enableTrigger = new HashSet<>();
//        private boolean remove;
//        private QueryExpression query;
//        private UpdateExpression update;
//        private ConfigurationNode queryNode;
//        private ConfigurationNode updateNode;
//
//        private Builder(){
//
//        }
//
//        public CheckRule build(){
//
//        }
//
//
//    }

    private final String name;

    private int priority = 5;
    private String ignorePermission = "";
    private Set<String> enableWorlds = new HashSet<>();
    private Set<String> enableTrigger = EpicBanItem.plugin.getSettings().getEnabledDefaultTriggers();

    private QueryExpression query;
    private ConfigurationNode queryNode;
    private @Nullable UpdateExpression update = null;
    private @Nullable ConfigurationNode updateNode = null;

    public CheckRule(String ruleName) {
        this(ruleName, getDefaultQueryNode());
    }

    public CheckRule(String ruleName, ConfigurationNode queryNode) {
        this(ruleName, queryNode, getDefaultUpdateNode());
    }

    public CheckRule(String ruleName, ConfigurationNode queryNode, @Nullable ConfigurationNode updateNode) {
        this.queryNode = queryNode.copy();
        this.query = new QueryExpression(queryNode);
        this.name = Objects.requireNonNull(ruleName);
        this.updateNode = Objects.isNull(updateNode) ? null : updateNode.copy();
        this.update = Objects.isNull(updateNode) ? null : new UpdateExpression(updateNode);
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public Set<String> getEnableTrigger() {
        return Collections.unmodifiableSet(enableTrigger);
    }

    public Set<String> getEnableWorlds() {
        return Collections.unmodifiableSet(enableWorlds);
    }

    public Text getQueryInfo() {
        try {
            return Text.of(TextUtil.deserializeConfigNodeToString(queryNode));
        } catch (IOException e) {
            EpicBanItem.logger.error("Failed to deserialize ConfigNode to String", e);
            return EpicBanItem.plugin.getMessages().getMessage("epicbanitem.error.failDeserialize");
        }
    }

    public Text getUpdateInfo() {
        if (updateNode == null) {
            return Text.of("No Update");
        }
        try {
            return Text.of(TextUtil.deserializeConfigNodeToString(updateNode));
        } catch (IOException e) {
            EpicBanItem.logger.error("Failed to deserialize ConfigNode to String", e);
            return EpicBanItem.plugin.getMessages().getMessage("epicbanitem.error.failDeserialize");
        }
    }

    /**
     * @param item    被检查的物品
     * @param world   检查发生世界名
     * @param trigger 检查发生trigger
     * @param subject 被检查的权限主体
     * @return 检查结果
     */
    public CheckResult check(ItemStack item, CheckResult origin, World world, String trigger, @Nullable Subject subject) {
        return check(NbtTagDataUtil.toNbt(item), origin, world, trigger, subject);
    }

    /**
     * @param view    被检查的物品
     * @param world   检查发生世界名
     * @param trigger 检查发生trigger
     * @param subject 被检查的权限主体
     * @return 检查结果
     */
    public CheckResult check(DataView view, CheckResult origin, World world, String trigger, @Nullable Subject subject) {
        if (!enableTrigger.contains(trigger)) {
            return origin;
        }
        if (!enableWorlds.isEmpty() && !enableWorlds.contains(world.getName())) {
            return origin;
        }
        if (!ignorePermission.isEmpty() && subject != null && subject.hasPermission(ignorePermission)) {
            return origin;
        }

        Optional<QueryResult> optionalQueryResult = query.query(DataQuery.of(), view);
        if (optionalQueryResult.isPresent()) {
            origin.breakRules.add(this);
            if (update != null) {
                update.update(optionalQueryResult.get(), view).apply(view);
                origin.view = view;
            }
        }
        return origin;
    }

    public Text toText() {
        Messages messages = EpicBanItem.plugin.getMessages();
        Text.Builder builder = Text.builder();
        builder.append(Text.of(this.getName()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.worlds"), Text.of(this.getEnableWorlds().toString()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.triggers"), Text.of(this.getEnableWorlds().toString()), Text.NEW_LINE);
//        builder.append(messages.getMessage("epicbanitem.checkrule.query"),this.getQueryInfo(),Text.NEW_LINE);
//        builder.append(messages.getMessage("epicbanitem.checkrule.update"),this.getUpdateInfo(),Text.NEW_LINE);
        return Text.builder(getName()).onHover(TextActions.showText(builder.build())).build();
    }

    public Text info() {
        // TODO: 点击补全指令?
        Messages messages = EpicBanItem.plugin.getMessages();
        Text.Builder builder = Text.builder();
        builder.append(Text.of(this.getName()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.worlds"), Text.of(this.getEnableWorlds().toString()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.triggers"), Text.of(this.getEnableWorlds().toString()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.query"), this.getQueryInfo(), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.update"), this.getUpdateInfo(), Text.NEW_LINE);
        return builder.build();
    }

    private static ConfigurationNode getDefaultQueryNode() {
        try {
            return TextUtil.serializeStringToConfigNode("{}");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ConfigurationNode getDefaultUpdateNode() {
        try {
            return TextUtil.serializeStringToConfigNode("{\"$set\": {id: \"minecraft:air\"}}");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Serializer implements TypeSerializer<CheckRule> {

        @Override
        public CheckRule deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            CheckRule rule = new CheckRule(node.getNode("name").getString());
            rule.priority = node.getNode("priority").getInt(5);
            rule.ignorePermission = node.getNode("bypass-permissions").getString("");
            rule.enableWorlds.addAll(node.getNode("enabled-worlds").getList(TypeToken.of(String.class), Collections.emptyList()));
            ConfigurationNode triggerNode = node.getNode("use-trigger");
            rule.enableTrigger = new HashSet<>(EpicBanItem.plugin.getSettings().getEnabledDefaultTriggers());
            triggerNode.getChildrenMap().forEach((k, v) -> {
                if (v.getBoolean()) {
                    rule.enableTrigger.add(k.toString());
                } else {
                    rule.enableTrigger.remove(k.toString());
                }
            });
            ConfigurationNode queryNode = node.getNode("query");
            ConfigurationNode updateNode = node.getNode("update");
            if (Objects.nonNull(queryNode.getValue())) {
                rule.queryNode = queryNode.copy();
                rule.query = new QueryExpression(rule.queryNode);
            }
            if (Objects.nonNull(updateNode.getValue())) {
                rule.updateNode = updateNode.copy();
                rule.update = new UpdateExpression(rule.updateNode);
            } else if (!node.getNode("remove").getBoolean(false)) {
                rule.updateNode = null;
                rule.update = null;
            }
            return rule;
        }

        @Override
        public void serialize(TypeToken<?> type, CheckRule rule, ConfigurationNode node) {
            node.getNode("name").setValue(rule.name);
            node.getNode("priority").setValue(rule.priority);
            node.getNode("bypass-permissions").setValue(rule.ignorePermission);
            if (!rule.enableWorlds.isEmpty()) {
                for (String world : rule.enableWorlds) {
                    node.getNode("enabled-worlds").getAppendedNode().setValue(world);
                }
            }
            for (String trigger : EpicBanItem.plugin.getSettings().getEnabledDefaultTriggers()) {
                node.getNode("use-trigger", trigger).setValue(rule.enableTrigger.contains(trigger));
            }
            for (String trigger : EpicBanItem.plugin.getSettings().getDisabledDefaultTriggers()) {
                node.getNode("use-trigger", trigger).setValue(rule.enableTrigger.contains(trigger));
            }
            node.getNode("query").setValue(rule.queryNode);
            node.getNode("update").setValue(rule.updateNode);
        }
    }
}
