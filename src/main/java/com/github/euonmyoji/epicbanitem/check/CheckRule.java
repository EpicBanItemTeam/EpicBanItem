package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.configuration.BanItemConfig;
import com.github.euonmyoji.epicbanitem.configuration.Settings;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
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
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author GINYAI yinyangshi
 */
public class CheckRule {
    private String name;
    private Set<String> enableWorlds;
    private String ignorePermission;
    private Set<String> enableTrigger;
    private boolean remove;
    private QueryExpression query;
    private UpdateExpression update;
    private ConfigurationNode queryNode;
    private ConfigurationNode updateNode;

    //todo:Builder
    private CheckRule(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
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
        if (enableWorlds != null && !enableWorlds.contains(world.getName())) {
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
        throw new UnsupportedOperationException("TODO");
    }

    static {
        TypeSerializers.getDefaultSerializers().registerType(BanItemConfig.CHECK_RULE_TYPE_TOKEN, new Serializer());
    }

    public static class Serializer implements TypeSerializer<CheckRule> {

        @Override
        public CheckRule deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            CheckRule rule = new CheckRule(node.getNode("name").getString());
            rule.ignorePermission = node.getNode("bypass-permissions").getString(null);
            if (!node.getNode("enabled-worlds").isVirtual()) {
                rule.enableWorlds = new HashSet<>(node.getNode("enabled-worlds").getList(TypeToken.of(String.class)));
                //有必要么
                if (rule.enableWorlds.size() == 0) {
                    rule.enableWorlds = null;
                }
            }
            ConfigurationNode triggerNode = node.getNode("use-trigger");
            rule.enableTrigger = new HashSet<>();
            for (Map.Entry<String, Boolean> entry : Settings.getDefaultTriggers().entrySet()) {
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
            for (String trigger : Settings.getDefaultTriggers().keySet()) {
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
