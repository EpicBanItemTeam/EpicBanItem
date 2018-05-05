package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class BanItemConfig {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static final TypeToken<CheckRule> CHECK_RULE_TYPE_TOKEN = TypeToken.of(CheckRule.class);

    private BanItemConfig() {
        //nothing here
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(EpicBanItem.plugin.cfgDir.resolve("banitem.conf")).build();
        cfg.getNode("config-version").setValue(cfg.getNode("config-version").getInt(1));
        reload();
    }

    /**
     * @param type 适用于那个规则的物品type
     * @param rule 规则
     * @return true if added successful and false anywise else
     * @throws IllegalArgumentException if the rule is already apply to this itemtype
     * @throws IOException              if ioe ...
     */
    public static boolean addCheckRule(ItemType type, CheckRule rule) throws IOException, ObjectMappingException {
        if (isTypeAppliedRule(type, rule)) {
            throw new IllegalArgumentException("The rule is already apply to this itemtype!");
        }
        cfg.getNode("epicbanitem", type.getId(), rule.getName()).setValue(CHECK_RULE_TYPE_TOKEN, rule);
        return save();
    }

    public static boolean removeCheckRule(ItemType type, String ruleName) throws IOException {
        return cfg.getNode("epicbanitem", type.getId()).removeChild(ruleName) && save();
    }

    /**
     * @param type type
     * @return list or a empty list if the itemtype doesn't apply any rule!
     */
    public static List<CheckRule> getItemtypeApplyRules(ItemType type) {
        Map<Object, ? extends CommentedConfigurationNode> map = getBanMap();
        if (map.containsKey(type)) {
            List<CheckRule> list = new ArrayList<>();
            for (CommentedConfigurationNode commentedConfigurationNode : map.get(type).getChildrenList()) {
                try {
                    list.add(commentedConfigurationNode.getValue(CHECK_RULE_TYPE_TOKEN));
                } catch (ObjectMappingException e) {
                    EpicBanItem.logger.error(String.format("物品类型%s配置文件格式错误!", type.getId()), e);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    public static void reload() {
        cfg = load();
    }

    public static Set<ItemType> bannedItemType() {
        return getBanMap().keySet().stream()
                .map(o -> Sponge.getRegistry().getType(ItemType.class, (String) o))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public static CommentedConfigurationNode getCfg() {
        return cfg;
    }

    private static boolean isTypeAppliedRule(ItemType type, CheckRule rule) {
        Map<Object, ? extends CommentedConfigurationNode> map = cfg.getNode("epicbanitem").getChildrenMap();
        return !map.get(type.getName()).getNode(rule.getName()).isVirtual();
    }

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    private static Map<Object, ? extends CommentedConfigurationNode> getBanMap() {
        return cfg.getNode("epicbanitem").getChildrenMap();
    }

    private static boolean save() throws IOException {
        loader.save(cfg);
        return true;
    }
}
