package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@NonnullByDefault
public class BanConfig {
    public static final int CURRENT_VERSION = 1;
    public static final TypeToken<CheckRule> RULE_TOKEN = TypeToken.of(CheckRule.class);
    public static final Comparator<CheckRule> COMPARATOR = Comparator.comparing(CheckRule::getPriority);

    private final Path path;
    private final AutoFileLoader fileLoader;
    private final ListMultimap<String, CheckRule> rules;
    private final Set<ItemType> items = new TreeSet<>(Comparator.comparing(ItemType::getId));

    public BanConfig(AutoFileLoader fileLoader, Path path) {
        this.path = path;
        this.fileLoader = fileLoader;
        fileLoader.addListener(path, this::load, this::save);
        EpicBanItem.logger.debug("Generating Item to Block mapping: ");
        NbtTagDataUtil.printLog().forEachRemaining(EpicBanItem.logger::debug);
        this.rules = Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new);
        TypeSerializers.getDefaultSerializers().registerType(BanConfig.RULE_TOKEN, new CheckRule.Serializer());
    }

    public Set<ItemType> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public List<CheckRule> getRules(ItemType itemType) {
        return Collections.unmodifiableList(rules.get(itemType.getId()));
    }

    public void addRule(ItemType type, CheckRule newRule) throws IOException {
        items.add(type);
        String typeId = type.getId();
        rules.putAll(typeId, addAndSort(rules.removeAll(typeId), newRule));
        fileLoader.forceSaving(path);
    }

    // TODO: 出现错误暂时捕获 加载完全部之后再抛出? 或者返回一个布尔值表示十分出错?

    private void load(ConfigurationNode node) throws IOException {
        // noinspection unused
        int version = node.getNode("epicbanitem-version").<Integer>getValue(Types::asInt, () -> {
            EpicBanItem.logger.warn("Ban Config at {} is missing epicbanitem-version option.", path);
            EpicBanItem.logger.warn("Try loading using current version {}.", CURRENT_VERSION);
            return CURRENT_VERSION;
        });
        try {
            rules.clear();
            items.clear();
            Map<Object, ? extends ConfigurationNode> checkRules = node.getNode("epicbanitem").getChildrenMap();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : checkRules.entrySet()) {
                String item = entry.getKey().toString();
                Sponge.getRegistry().getType(ItemType.class, item).ifPresent(items::add);
                rules.putAll(item, entry.getValue().getList(RULE_TOKEN).stream().sorted(COMPARATOR)::iterator);
            }
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
    }

    private void save(ConfigurationNode node) throws IOException {
        node.getNode("epicbanitem-version").setValue(CURRENT_VERSION);
        try {
            for (Map.Entry<String, CheckRule> entry : rules.entries()) {
                node.getNode("epicbanitem", entry.getKey()).getAppendedNode().setValue(RULE_TOKEN, entry.getValue());
            }
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
    }

    private static List<CheckRule> addAndSort(List<CheckRule> original, CheckRule newCheckRule) throws IOException {
        CheckRule[] newCheckRules;
        int ruleSize = original.size();
        newCheckRules = new CheckRule[ruleSize + 1];
        newCheckRules[ruleSize] = newCheckRule;
        for (int i = 0; i < ruleSize; ++i) {
            CheckRule checkRule = original.get(i);
            if (checkRule.getName().equals(newCheckRule.getName())) {
                throw new IOException("Rule with the same name already exits");
            }
            newCheckRules[i] = checkRule;
        }
        Arrays.sort(newCheckRules, COMPARATOR);
        return Arrays.asList(newCheckRules);
    }
}
