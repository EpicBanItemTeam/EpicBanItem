package com.github.euonmyoji.epicbanitem.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleIndex;
import com.google.common.base.MoreObjects;
import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class BanConfig {
    static final int CURRENT_VERSION = 1;
    private static final TypeToken<CheckRule> RULE_TOKEN = TypeToken.of(CheckRule.class);
    private static final Comparator<String> RULE_NAME_COMPARATOR = Comparator.naturalOrder();
    private static final Comparator<CheckRule> COMPARATOR = Comparator.comparing(CheckRule::getPriority).thenComparing(CheckRule::getName, RULE_NAME_COMPARATOR);

    private final Path path;
    private final AutoFileLoader fileLoader;
    private ImmutableSortedMap<String, CheckRule> checkRulesByName;
    private ImmutableListMultimap<CheckRuleIndex, CheckRule> checkRulesByIndex;
    private final LoadingCache<String, ImmutableList<CheckRule>> cacheFromIdToCheckRules;

    public BanConfig(AutoFileLoader fileLoader, Path path) {
        this.path = path;
        this.fileLoader = fileLoader;

        this.checkRulesByIndex = ImmutableListMultimap.of();
        this.checkRulesByName = ImmutableSortedMap.<String, CheckRule>orderedBy(RULE_NAME_COMPARATOR).build();

        this.cacheFromIdToCheckRules = Caffeine.newBuilder().build(k -> {
            CheckRuleIndex i = CheckRuleIndex.of(), j = CheckRuleIndex.of(k);
            Iterable<? extends List<CheckRule>> rules = Arrays.asList(getRules(i), getRules(j));
            Stream<CheckRule> stream = Streams.stream(Iterables.mergeSorted(rules, getComparator()));
            return stream.filter(r -> r.idIndexFilter().test(k)).collect(ImmutableList.toImmutableList());
        });

        TypeSerializers.getDefaultSerializers().registerType(BanConfig.RULE_TOKEN, new CheckRule.Serializer());

        fileLoader.addListener(path, this::load, this::save);
        if (Files.notExists(path)) {
            fileLoader.forceSaving(path, n -> n.getNode("epicbanitem-version").setValue(CURRENT_VERSION).getParent());
        }
    }

    private static int parseOrElse(String string, int orElse) {
        try {
            return Integer.parseUnsignedInt(string);
        } catch (NumberFormatException e) {
            return orElse;
        }
    }

    private static String findNewName(@Nullable String name, Predicate<String> alreadyExists) {
        if (name == null) {
            name = "undefined-1";
        }
        name = name.toLowerCase();
        if (!CheckRule.NAME_PATTERN.matcher(name).matches()) {
            name = "unrecognized-1";
        }
        if (alreadyExists.test(name)) {
            int defNumber = 2;
            int dashIndex = name.lastIndexOf('-');
            int number = parseOrElse(name.substring(dashIndex + 1), defNumber);
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

    public Comparator<CheckRule> getComparator() {
        return COMPARATOR;
    }

    public Set<CheckRuleIndex> getItems() {
        return Objects.requireNonNull(checkRulesByIndex).keySet();
    }

    public List<CheckRule> getRulesWithIdFiltered(String id) {
        return MoreObjects.firstNonNull(cacheFromIdToCheckRules.get(id), ImmutableList.of());
    }

    public List<CheckRule> getRules(CheckRuleIndex index) {
        return Objects.requireNonNull(checkRulesByIndex).get(index);
    }

    public Collection<CheckRule> getRules() {
        return checkRulesByName.values();
    }

    public Set<String> getRuleNames() {
        return checkRulesByName.keySet();
    }

    public Optional<CheckRule> getRule(String name) {
        return Optional.ofNullable(checkRulesByName.get(name));
    }

    public CompletableFuture<Boolean> addRule(CheckRuleIndex index, CheckRule newRule) throws IOException {
        try {
            SortedMap<String, CheckRule> rulesByName = new TreeMap<>(checkRulesByName);

            if (rulesByName.put(newRule.getName(), newRule) != null) {
                return CompletableFuture.completedFuture(Boolean.FALSE);
            }

            ListMultimap<CheckRuleIndex, CheckRule> rules;
            rules = Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new);

            rules.putAll(this.checkRulesByIndex);
            rules.putAll(index, addAndSort(rules.removeAll(index), newRule));

            this.checkRulesByIndex = ImmutableListMultimap.copyOf(rules);
            this.checkRulesByName = ImmutableSortedMap.copyOfSorted(rulesByName);
            this.cacheFromIdToCheckRules.invalidateAll();

            forceSave();
            return CompletableFuture.completedFuture(Boolean.TRUE);
            // TODO: return CompletableFuture from forceSave
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public CompletableFuture<Boolean> removeRule(@SuppressWarnings("unused todo: why") CheckRuleIndex index, String name) throws IOException {
        try {
            CheckRule rule = checkRulesByName.get(name);
            if (rule != null) {
                SortedMap<String, CheckRule> rulesByName = new TreeMap<>(checkRulesByName);
                rulesByName.remove(name);
                ImmutableListMultimap.Builder<CheckRuleIndex, CheckRule> builder = ImmutableListMultimap.builder();
                checkRulesByIndex.forEach((s, rule1) -> {
                    if (!rule1.getName().equals(name)) {
                        builder.put(s, rule1);
                    }
                });
                this.checkRulesByIndex = builder.build();
                this.checkRulesByName = ImmutableSortedMap.copyOfSorted(rulesByName);
                this.cacheFromIdToCheckRules.invalidateAll();

                forceSave();
                return CompletableFuture.completedFuture(Boolean.TRUE);
                // TODO: return CompletableFuture from forceSave
            } else {
                return CompletableFuture.completedFuture(Boolean.FALSE);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void load(ConfigurationNode node) throws IOException {
        // noinspection unused
        int version = node.getNode("epicbanitem-version").<Integer>getValue(Types::asInt, () -> {
            EpicBanItem.getLogger().warn("Ban Config at {} is missing epicbanitem-version option.", path);
            EpicBanItem.getLogger().warn("Try loading using current version {}.", CURRENT_VERSION);
            return CURRENT_VERSION;
        });
        try {
            SortedMap<String, CheckRule> rulesByName = new TreeMap<>(RULE_NAME_COMPARATOR);
            Multimap<CheckRuleIndex, CheckRule> rulesByItem = Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new);
            Map<Object, ? extends ConfigurationNode> checkRules = node.getNode("epicbanitem").getChildrenMap();
            boolean needSave = false;
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : checkRules.entrySet()) {
                String item = entry.getKey().toString();
                List<CheckRule> ruleList = new ArrayList<>();
                for (ConfigurationNode node1 : entry.getValue().getChildrenList()) {
                    ConfigurationNode originNode = node1.copy();
                    //check name before TypeSerializer of CheckRule
                    String name = node1.getNode("name").getString();
                    if (name == null || !CheckRule.NAME_PATTERN.matcher(name).matches() || rulesByName.containsKey(name)) {
                        String newName = findNewName(name, rulesByName::containsKey);
                        node1.getNode("name").setValue(newName);
                        EpicBanItem.getLogger().warn("Find duplicate or illegal name,temporarily renamed \"{}\" in {} to \"{}\"", name, item, newName);
                    }
                    CheckRule rule = Objects.requireNonNull(node1.getValue(RULE_TOKEN));
                    rule.setConfigurationNode(originNode);
                    // fix id
                    if (!"*".equals(item)) {
                        needSave = rule.tryFixId(item) || needSave;
                    }
                    rulesByName.put(rule.getName(), rule);
                    ruleList.add(rule);
                }
                ruleList.sort(COMPARATOR);
                rulesByItem.putAll(CheckRuleIndex.of(item), ruleList);
            }
            this.checkRulesByIndex = ImmutableListMultimap.copyOf(rulesByItem);
            this.checkRulesByName = ImmutableSortedMap.copyOfSorted(rulesByName);
            this.cacheFromIdToCheckRules.invalidateAll();
            if (needSave) {
                forceSave();
            }
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
    }

    private void forceSave() {
        this.fileLoader.forceSaving(this.path);
    }

    private void save(ConfigurationNode node) throws IOException {
        node.getNode("epicbanitem-version").setValue(CURRENT_VERSION);
        try {
            for (Map.Entry<CheckRuleIndex, CheckRule> entry : checkRulesByIndex.entries()) {
                node.getNode("epicbanitem", entry.getKey().toString()).getAppendedNode().setValue(RULE_TOKEN, entry.getValue());
            }
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
    }
}
