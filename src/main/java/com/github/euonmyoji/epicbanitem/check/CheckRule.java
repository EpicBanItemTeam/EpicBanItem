package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.configuration.BanItemConfig;
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
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

/**
 * @author GiNYAi yinyangshi
 */
public class CheckRule {
    private String name;
    private Set<String> enableWorlds = new HashSet<>();
    private String ignorePermission;
    private Set<String> enableTrigger = new HashSet<>();
    private boolean remove;
    private QueryExpression query;
    private UpdateExpression update;
    private ConfigurationNode queryNode;
    private ConfigurationNode updateNode;

    private CheckRule(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public Set<String> getEnableTrigger() {
        return Collections.unmodifiableSet(enableTrigger);
    }

    public Set<String> getEnableWorlds() {
        return Collections.unmodifiableSet(enableWorlds);
    }

    public boolean remove() {
        return remove;
    }

    public Text getQueryInfo() {
        if (queryNode == null) {
            return Text.of("No Query");
        }
        try {
            return Text.of(TextUtil.deserializeConfigNodeToString(queryNode));
        } catch (IOException e) {
            EpicBanItem.logger.error("Failed to deserialize cConfigNode to String", e);
            //todo:翻译 "Failed to deserialize" 的 key
            return EpicBanItem.plugin.getMessages().getMessage("key");
        }
    }

    public Text getUpdateInfo() {
        if (updateNode == null) {
            return Text.of("No Update");
        }
        try {
            return Text.of(TextUtil.deserializeConfigNodeToString(updateNode));
        } catch (IOException e) {
            EpicBanItem.logger.error("Failed to deserialize cConfigNode to String", e);
            //todo:翻译 "Failed to deserialize" 的 key
            return EpicBanItem.plugin.getMessages().getMessage("key");
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
    private CheckResult check(DataView view, CheckResult origin, World world, String trigger, @Nullable Subject subject) {
        if (!enableTrigger.contains(trigger)) {
            return origin;
        }
        if (!enableWorlds.isEmpty() && !enableWorlds.contains(world.getName())) {
            return origin;
        }
        if (ignorePermission != null && subject != null && subject.hasPermission(ignorePermission)) {
            return origin;
        }

        if (query == null) {
            origin.breakRules.add(this);
            origin.remove = origin.remove || remove;
        } else {
            Optional<QueryResult> optionalQueryResult = query.query(DataQuery.of(), view);
            if (optionalQueryResult.isPresent()) {
                origin.breakRules.add(this);
                if (remove) {
                    origin.remove = true;
                } else if (update != null) {
                    update.update(optionalQueryResult.get(), view).apply(view);
                    //todo:返回ItemStack 而不是DataView？
                    origin.view = view;
                }
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
        builder.append(messages.getMessage("epicbanitem.checkrule.remove"), Text.of(this.remove()), Text.NEW_LINE);
//        builder.append(messages.getMessage("epicbanitem.checkrule.query"),this.getQueryInfo(),Text.NEW_LINE);
//        builder.append(messages.getMessage("epicbanitem.checkrule.update"),this.getUpdateInfo(),Text.NEW_LINE);
        return Text.builder(getName()).onHover(TextActions.showText(builder.build())).build();
    }

    public Text info() {
        //todo:点击补全指令?
        Messages messages = EpicBanItem.plugin.getMessages();
        Text.Builder builder = Text.builder();
        builder.append(Text.of(this.getName()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.worlds"), Text.of(this.getEnableWorlds().toString()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.triggers"), Text.of(this.getEnableWorlds().toString()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.remove"), Text.of(this.remove()), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.query"), this.getQueryInfo(), Text.NEW_LINE);
        builder.append(messages.getMessage("epicbanitem.checkrule.update"), this.getUpdateInfo(), Text.NEW_LINE);
        return builder.build();
    }

    static {
        TypeSerializers.getDefaultSerializers().registerType(BanItemConfig.CHECK_RULE_TYPE_TOKEN, new Serializer());
    }

    public static class Serializer implements TypeSerializer<CheckRule> {

        @Override
        public CheckRule deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            CheckRule rule = new CheckRule(node.getNode("name").getString());
            rule.ignorePermission = node.getNode("bypass-permissions").getString(null);
            rule.enableWorlds.addAll(node.getNode("enabled-worlds").getList(TypeToken.of(String.class), Collections.emptyList()));
            ConfigurationNode triggerNode = node.getNode("use-trigger");
            rule.enableTrigger = new HashSet<>();
            for (Map.Entry<String, Boolean> entry : EpicBanItem.plugin.getSettings().getDefaultTriggers().entrySet()) {
                if (triggerNode.getNode(entry.getKey()).getBoolean(entry.getValue())) {
                    rule.enableTrigger.add(entry.getKey());
                }
            }
            ConfigurationNode queryNode = node.getNode("query");
            if (!queryNode.isVirtual()) {
                rule.query = new QueryExpression(queryNode);
                rule.queryNode = queryNode;
            }
            ConfigurationNode updateNode = node.getNode("update");
            if (!updateNode.isVirtual()) {
                rule.update = new UpdateExpression(updateNode);
                rule.updateNode = updateNode;
            }
            rule.remove = node.getNode("remove").getBoolean(rule.update == null);
            return rule;
        }

        @Override
        public void serialize(TypeToken<?> type, CheckRule rule, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("name").setValue(rule.name);
            node.getNode("bypass-permissions").setValue(rule.ignorePermission);
            if (rule.enableWorlds != null) {
                node.getNode("enabled-worlds").setValue(new TypeToken<List<String>>() {
                }, new ArrayList<>(rule.enableWorlds));
            }
            for (String trigger : EpicBanItem.plugin.getSettings().getDefaultTriggers().keySet()) {
                node.getNode("use-trigger", trigger).setValue(rule.enableTrigger.contains(trigger));
            }
            if (rule.query != null) {
                node.getNode("query").setValue(rule.queryNode);
            }
            if (rule.update != null) {
                node.getNode("update").setValue(rule.updateNode);
            }
            node.getNode("remove").setValue(rule.remove);
        }
    }
}
