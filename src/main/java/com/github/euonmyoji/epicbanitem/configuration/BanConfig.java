package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class BanConfig {
    public static final TypeToken<CheckRule> RULE_TOKEN = TypeToken.of(CheckRule.class);
    private boolean editable;
    private Path path;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode node;

    private Map<String,List<CheckRule>> rules;

    public BanConfig(Path path) throws IOException {
        this.path = path;
        this.loader = HoconConfigurationLoader.builder().setPath(path).build();
        this.node = loader.load();
    }

    public Map<String, List<CheckRule>> getRules() {
        return rules;
    }

    //todo:何时加载
    //todo:出现错误暂时捕获 加载完全部之后再抛出? 或者返回一个布尔值表示十分出错?
    public void reload() throws ObjectMappingException {
        rules = new LinkedHashMap<>();
        for(Map.Entry<Object,? extends CommentedConfigurationNode> entry:node.getNode("epicbanitem").getChildrenMap().entrySet()){
            rules.put(entry.getKey().toString(),entry.getValue().getList(RULE_TOKEN));
        }
        if(!editable){
            rules = Collections.unmodifiableMap(rules);
        }
    }

    //todo:先备份再保存?
    public void save() throws IOException, ObjectMappingException {
        if(editable){
            node.getNode("epicbanitem").setValue(new TypeToken<Map<String,List<CheckRule>>>() {},rules);
            loader.save(node);
        }
    }




}
