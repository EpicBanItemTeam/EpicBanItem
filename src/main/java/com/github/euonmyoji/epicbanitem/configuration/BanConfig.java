package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class BanConfig {
    public static final int CURRENT_VERSION = 1;
    public static final TypeToken<CheckRule> RULE_TOKEN = TypeToken.of(CheckRule.class);
    private boolean editable;
    private Path path;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode node;

    private Map<String, List<CheckRule>> rules;

    public BanConfig(Path path) {
        this(path, false);
    }

    public BanConfig(Path path, boolean editable) {
        this.path = path;
        this.loader = HoconConfigurationLoader.builder().setPath(path).build();
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean addRule(ItemType type, CheckRule rule) throws IOException, ObjectMappingException {
        if (!editable) {
            return false;
        }
        rule.setSource(this);
        String typeId = type.getId();
        if (!rules.containsKey(typeId)) {
            rules.put(typeId, Lists.newArrayList());
        }
        List<CheckRule> ruleList = rules.get(typeId);
        ruleList.add(rule);
        save();
        return true;
    }

    public Map<String, List<CheckRule>> getRules() {
        return rules;
    }

    //todo:何时加载
    //todo:出现错误暂时捕获 加载完全部之后再抛出? 或者返回一个布尔值表示十分出错?

    public void reload() throws ObjectMappingException, IOException {
        this.node = loader.load();
        Integer version = node.getNode("epicbanitem-version").getValue(TypeToken.of(Integer.class));
        if (version == null) {
            EpicBanItem.logger.warn("Ban Config at {} is missing epicbanitem-version,try loading using current version {}.", path, CURRENT_VERSION);
            version = CURRENT_VERSION;
            node.getNode("epicbanitem-version").setValue(version);
        }
        //todo:load according to version
        rules = new LinkedHashMap<>();
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : node.getNode("epicbanitem").getChildrenMap().entrySet()) {
            rules.put(entry.getKey().toString(), entry.getValue().getList(RULE_TOKEN));
        }
        if (editable) {
            save();
        } else {
            rules = Collections.unmodifiableMap(rules);
        }
    }

    //todo:先备份再保存?

    public void save() throws IOException, ObjectMappingException {
        if (editable) {
            node.getNode("epicbanitem").setValue(new TypeToken<Map<String, List<CheckRule>>>() {
            }, rules);
            loader.save(node);
        }
    }

    public static Map<ItemType, List<CheckRule>> findType(Map<String, List<CheckRule>> rules) {
        Map<ItemType, List<CheckRule>> map = new HashMap<>(rules.size());
        for (Map.Entry<String, List<CheckRule>> entry : rules.entrySet()) {
            Optional<ItemType> optionalItemType = Sponge.getRegistry().getType(ItemType.class, entry.getKey());
            if (optionalItemType.isPresent()) {
                map.put(optionalItemType.get(), entry.getValue());
            } else {
                EpicBanItem.logger.error("Cannot find item type :" + entry.getKey(), ",rules for it won't load.");
            }
        }
        return map;
    }

    public BanConfig setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }
}
