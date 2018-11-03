package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

@NonnullByDefault
public class BanConfig {
    public static final int CURRENT_VERSION = 1;
    public static final TypeToken<CheckRule> RULE_TOKEN = TypeToken.of(CheckRule.class);
    public static final Comparator<CheckRule> COMPARATOR = Comparator.comparing(CheckRule::getPriority);
    public static final Comparator<ItemType> ITEM_TYPE_COMPARATOR = Comparator.comparing(ItemType::getId);

    private final Path path;
    private final AutoFileLoader fileLoader;
    private ImmutableMap<String, CheckRule> checkRulesByName = ImmutableMap.of();
    private ImmutableListMultimap<String, CheckRule> checkRulesByItem = ImmutableListMultimap.of();
    private ImmutableSortedSet<ItemType> itemTypes = ImmutableSortedSet.orderedBy(ITEM_TYPE_COMPARATOR).build();

    public BanConfig(AutoFileLoader fileLoader, Path path) {
        this.path = path;
        this.fileLoader = fileLoader;

        fileLoader.addListener(path, this::load, this::save);
        if (Files.notExists(path)) {
            fileLoader.forceSaving(path, n -> n.getNode("epicbanitem-version").setValue(CURRENT_VERSION).getParent());
        }

        NbtTagDataUtil.printToLogger(EpicBanItem.logger::debug);
        TypeSerializers.getDefaultSerializers().registerType(BanConfig.RULE_TOKEN, new CheckRule.Serializer());
    }

    public Set<ItemType> getItems() {
        return Objects.requireNonNull(itemTypes);
    }

    public List<CheckRule> getRules(@Nullable ItemType itemType) {
        return Objects.requireNonNull(checkRulesByItem).get(getTypeId(itemType));
    }

    public Collection<CheckRule> getRules(){
        return checkRulesByName.values();
    }

    public Set<String> getRuleNames() {
        return checkRulesByName.keySet();
    }

    public Optional<CheckRule> getRule(String name) {
        return Optional.ofNullable(checkRulesByName.get(name));
    }

    public void addRule(@Nullable ItemType itemType, CheckRule newRule) throws IOException {
        try {
            ListMultimap<String, CheckRule> newRules = Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new);
            SortedSet<ItemType> newItems = new TreeSet<>(this.itemTypes);
            newRules.putAll(this.checkRulesByItem);

            String typeId = getTypeId(itemType);
            Optional.ofNullable(itemType).ifPresent(newItems::add);
            newRules.putAll(typeId, addAndSort(newRules.removeAll(typeId), newRule));

            this.checkRulesByItem = ImmutableListMultimap.copyOf(newRules);
            this.itemTypes = ImmutableSortedSet.orderedBy(ITEM_TYPE_COMPARATOR).addAll(this.itemTypes).build();

            Map<String, CheckRule> rulesByName = new LinkedHashMap<>(checkRulesByName);
            rulesByName.put(newRule.getName(), newRule);
            this.checkRulesByName = ImmutableMap.copyOf(rulesByName);

            forceSave();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public boolean removeRule(String name) throws IOException {
        try {
            CheckRule rule = checkRulesByName.get(name);
            if(rule!=null){
                Map<String, CheckRule> rulesByName = new LinkedHashMap<>(checkRulesByName);
                rulesByName.remove(name);
                ImmutableListMultimap.Builder<String,CheckRule> builder = ImmutableListMultimap.builder();
                checkRulesByItem.forEach((s, rule1) -> {
                    if(!rule1.getName().equals(name)){
                        builder.put(s,rule1);
                    }
                });
                this.checkRulesByItem = builder.build();
                this.checkRulesByName = ImmutableMap.copyOf(rulesByName);

                forceSave();
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
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
            Map<String, CheckRule> rulesByName = new LinkedHashMap<>();
            Set<ItemType> newItems = new TreeSet<>(Comparator.comparing(ItemType::getId));
            Multimap<String, CheckRule> rulesByItem = Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new);
            Map<Object, ? extends ConfigurationNode> checkRules = node.getNode("epicbanitem").getChildrenMap();
            boolean needSave = false;
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : checkRules.entrySet()) {
                String item = entry.getKey().toString();
                Sponge.getRegistry().getType(ItemType.class, item).ifPresent(newItems::add);
                List<CheckRule> ruleList = new ArrayList<>();
                for (ConfigurationNode node1 : entry.getValue().getChildrenList()) {
                    CheckRule rule = node1.getValue(RULE_TOKEN);
                    if (rulesByName.containsKey(rule.getName())) {
                        String newName = findNewName(rule.getName(), rulesByName::containsKey);
                        rule = new CheckRule(newName, rule);
                        EpicBanItem.logger.warn("Find duplicate names {}, renamed one to {}", rule.getName(), newName);
                        needSave = true;
                    }
                    //fix id
                    if (!item.equals("*")) {
                        needSave = rule.tryFixId(item) || needSave;
                    }
                    rulesByName.put(rule.getName(), rule);
                    ruleList.add(rule);
                }
                ruleList.sort(COMPARATOR);
                rulesByItem.putAll(item, ruleList);
            }
            this.itemTypes = ImmutableSortedSet.copyOf(ITEM_TYPE_COMPARATOR, newItems);
            this.checkRulesByItem = ImmutableListMultimap.copyOf(rulesByItem);
            this.checkRulesByName = ImmutableMap.copyOf(rulesByName);
            if (needSave) {
                forceSave();
            }
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
    }

    public void forceSave(){
        this.fileLoader.forceSaving(this.path);
    }

    private void save(ConfigurationNode node) throws IOException {
        node.getNode("epicbanitem-version").setValue(CURRENT_VERSION);
        try {
            for (Map.Entry<String, CheckRule> entry : checkRulesByItem.entries()) {
                node.getNode("epicbanitem", entry.getKey()).getAppendedNode().setValue(RULE_TOKEN, entry.getValue());
            }
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
    }

    private static String getTypeId(@Nullable ItemType itemType) {
        return Objects.isNull(itemType) ? "*" : itemType.getId();
    }

    private static int parseOrElse(String string, int orElse) {
        try {
            return Integer.parseUnsignedInt(string);
        } catch (NumberFormatException e) {
            return orElse;
        }
    }

    private static String findNewName(String name, Predicate<String> alreadyExists) {
        if (alreadyExists.test(name)) {
            int dashIndex = name.lastIndexOf('-');
            int number = parseOrElse(name.substring(dashIndex + 1), 2);
            String prefix = dashIndex >= 0 ? name.substring(0, dashIndex) : name;
            for (name = prefix + '-' + number; alreadyExists.test(name); name = prefix + '-' + number) {
                ++number;
            }
        }
        return name;
    }

    private static List<CheckRule> addAndSort(List<CheckRule> original, CheckRule newCheckRule) {
        CheckRule[] newCheckRules;
        int ruleSize = original.size();
        newCheckRules = new CheckRule[ruleSize + 1];
        newCheckRules[ruleSize] = newCheckRule;
        for (int i = 0; i < ruleSize; ++i) {
            CheckRule checkRule = original.get(i);
            if (checkRule.getName().equals(newCheckRule.getName())) {
                throw new IllegalArgumentException("Rule with the same name already exits");
            }
            newCheckRules[i] = checkRule;
        }
        Arrays.sort(newCheckRules, COMPARATOR);
        return Arrays.asList(newCheckRules);
    }
}
