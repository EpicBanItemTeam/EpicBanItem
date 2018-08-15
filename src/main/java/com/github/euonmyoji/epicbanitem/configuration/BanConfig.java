package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.SimpleCheckRuleServiceImpl;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.Types;
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

    private final Path path;
    private final SimpleCheckRuleServiceImpl service;
    private final ListMultimap<String, CheckRule> rules;
    private final ConfigurationLoader<CommentedConfigurationNode> loader;

    private CommentedConfigurationNode node;

    public BanConfig(SimpleCheckRuleServiceImpl service, Path path) {
        this.path = path;
        this.service = service;
        this.loader = HoconConfigurationLoader.builder().setPath(path).build();
        this.rules = Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new);
    }

    public boolean addRule(ItemType type, CheckRule rule) throws IOException, ObjectMappingException {
        rule.setSource(this);
        rules.put(type.getId(), rule);
        save();
        return true;
    }

    //todo:何时加载
    //todo:出现错误暂时捕获 加载完全部之后再抛出? 或者返回一个布尔值表示十分出错?

    public void load() throws IOException {
        node = loader.load();
        // noinspection unused
        int version = node.getNode("epicbanitem-version").<Integer>getValue(Types::asInt, () -> {
            EpicBanItem.logger.warn("Ban Config at {} is missing epicbanitem-version option.", path);
            EpicBanItem.logger.warn("Try loading using current version {}.", CURRENT_VERSION);
            return CURRENT_VERSION;
        });
        try {
            rules.clear();
            Map<Object, ? extends CommentedConfigurationNode> checkRules = node.getNode("epicbanitem").getChildrenMap();
            for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : checkRules.entrySet()) {
                rules.putAll(entry.getKey().toString(), entry.getValue().getList(RULE_TOKEN));
            }
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
        service.clear();
        service.addRules(BanConfig.findType(Multimaps.asMap(rules)));
    }

    public void save() throws IOException {
        node.getNode("epicbanitem-version").setValue(CURRENT_VERSION);
        try {
            for (Map.Entry<String, CheckRule> entry : rules.entries()) {
                node.getNode("epicbanitem", entry.getKey()).setValue(RULE_TOKEN, entry.getValue());
            }
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
        loader.save(node);
    }

    private static Map<ItemType, List<CheckRule>> findType(Map<String, List<CheckRule>> rules) {
        Map<ItemType, List<CheckRule>> map = new HashMap<>(rules.size());
        for (Map.Entry<String, List<CheckRule>> entry : rules.entrySet()) {
            Optional<ItemType> optionalItemType = Sponge.getRegistry().getType(ItemType.class, entry.getKey());
            if (optionalItemType.isPresent()) {
                map.put(optionalItemType.get(), entry.getValue());
            } else {
                EpicBanItem.logger.error("Cannot find item type :" + entry.getKey(), ", rules for it won't load.");
            }
        }
        return map;
    }
}
