package com.github.euonmyoji.epicbanitem.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleIndex;
import com.github.euonmyoji.epicbanitem.configuration.update.UpdateService;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.file.ObservableDirectory;
import com.github.euonmyoji.epicbanitem.util.file.ObservableFileService;
import com.github.euonmyoji.epicbanitem.util.repackage.org.bstats.sponge.Metrics;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
@NonnullByDefault
@Singleton
// TODO: 2020/2/21 ConfigSerializable
// TODO: 2020/2/21 Logger I18N
public class BanConfig {
    private static final int CURRENT_VERSION = 2;
    private static final String MAIN_CONFIG_PATH = "banitem.conf";
    private static final String EXTRA_CONFIG_DIR = "ban_configs";
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("([a-z0-9-_]+)\\.conf");
    private static final TypeToken<CheckRule.Builder> RULE_BUILDER_TOKEN = TypeToken.of(CheckRule.Builder.class);
    private static final Comparator<String> RULE_NAME_COMPARATOR = Comparator.naturalOrder();
    private static final Comparator<CheckRule> COMPARATOR = Comparator
        .comparing(CheckRule::getPriority)
        .thenComparing(CheckRule::getName, RULE_NAME_COMPARATOR);

    private final Path mainPath;
    private final Path extraDir;
    private final Path configDir;

    @Inject
    private Logger logger;

    @Inject
    private Metrics metrics;

    @Inject
    private Injector injector;

    @Inject
    private UpdateService updateService;

    @Inject
    private ObservableFileService fileService;

    private ObservableConfigFile mainObservableFile;

    private final Map<String, ObservableConfigFile> extraObservableFiles;

    private final LoadingCache<String, ImmutableList<CheckRule>> cacheFromIdToCheckRules;
    private ImmutableSortedMap<String, CheckRule> checkRulesByName = ImmutableSortedMap.<String, CheckRule>orderedBy(RULE_NAME_COMPARATOR).build();
    private ImmutableListMultimap<CheckRuleIndex, CheckRule> checkRulesByIndex = ImmutableListMultimap.of();

    private final Set<String> allKnownIds = new LinkedHashSet<>();

    @Inject
    private BanConfig(@ConfigDir(sharedRoot = false) Path configDir, EventManager eventManager, PluginContainer pluginContainer) {
        this.mainPath = configDir.resolve(MAIN_CONFIG_PATH);
        this.extraDir = configDir.resolve(EXTRA_CONFIG_DIR).toAbsolutePath();
        this.configDir = configDir;
        this.extraObservableFiles = Maps.newHashMap();
        this.cacheFromIdToCheckRules =
            Caffeine
                .newBuilder()
                .build(
                    k -> {
                        CheckRuleIndex i = CheckRuleIndex.of(), j = CheckRuleIndex.of(k);
                        Iterable<? extends List<CheckRule>> rules = Arrays.asList(getRules(i), getRules(j));
                        Stream<CheckRule> stream = Streams.stream(Iterables.mergeSorted(rules, COMPARATOR));
                        return stream.filter(r -> r.idIndexFilter().test(k)).collect(ImmutableList.toImmutableList());
                    }
                );

        eventManager.registerListeners(pluginContainer, this);
    }

    public List<CheckRule> getRulesWithIdFiltered(String id) {
        //noinspection ConstantConditions
        return cacheFromIdToCheckRules.get(id);
    }

    public List<CheckRule> getRules(CheckRuleIndex index) {
        return checkRulesByIndex.get(index);
    }

    public Collection<CheckRule> getRules() {
        return checkRulesByName.values();
    }

    public Set<String> getRuleNames() {
        return checkRulesByName.keySet();
    }

    public Set<CheckRuleIndex> getItems() {
        return checkRulesByIndex.keySet();
    }

    public Optional<CheckRule> getRule(String name) {
        return Optional.ofNullable(checkRulesByName.get(name));
    }

    public CompletableFuture<Boolean> addRule(CheckRule newRule) throws IOException {
        try {
            SortedMap<String, CheckRule> rulesByName = new TreeMap<>(checkRulesByName);

            if (rulesByName.put(newRule.getName(), newRule) != null) {
                return CompletableFuture.completedFuture(Boolean.FALSE);
            }

            ImmutableListMultimap.Builder<CheckRuleIndex, CheckRule> byItem = ImmutableListMultimap
                .<CheckRuleIndex, CheckRule>builder()
                .orderKeysBy(Comparator.comparing(Object::toString))
                .orderValuesBy(COMPARATOR);

            byItem.putAll(this.checkRulesByIndex);

            Set<CheckRuleIndex> indices = getIndex(newRule);
            indices.forEach(index -> byItem.put(index, newRule));

            this.checkRulesByIndex = byItem.build();
            this.checkRulesByName = ImmutableSortedMap.copyOfSorted(rulesByName);
            this.cacheFromIdToCheckRules.invalidateAll();

            String name = newRule.getName();
            Matcher matcher = CheckRule.NAME_PATTERN.matcher(name);
            if (!matcher.matches()) {
                throw new IllegalStateException("Illegal rule name " + name);
            }
            String group = matcher.group("group");
            if (group == null) {
                mainObservableFile.save();
            } else {
                ObservableConfigFile observableConfigFile;
                if (!extraObservableFiles.containsKey(group)) {
                    observableConfigFile = addExtraObservable(group, false);
                } else {
                    observableConfigFile = extraObservableFiles.get(group);
                }
                observableConfigFile.save();
            }
            return CompletableFuture.completedFuture(Boolean.TRUE);
            // TODO: return CompletableFuture from forceSave
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public CompletableFuture<Boolean> removeRule(String name) throws IOException {
        try {
            CheckRule rule = checkRulesByName.get(name);
            if (rule != null) {
                SortedMap<String, CheckRule> rulesByName = new TreeMap<>(checkRulesByName);
                rulesByName.remove(name);
                ImmutableListMultimap.Builder<CheckRuleIndex, CheckRule> builder = ImmutableListMultimap.builder();
                checkRulesByIndex.forEach(
                    (s, rule1) -> {
                        if (!rule1.getName().equals(name)) {
                            builder.put(s, rule1);
                        }
                    }
                );
                this.checkRulesByIndex = builder.build();
                this.checkRulesByName = ImmutableSortedMap.copyOfSorted(rulesByName);
                this.cacheFromIdToCheckRules.invalidateAll();

                Matcher matcher = CheckRule.NAME_PATTERN.matcher(name);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Illegal rule name " + name);
                }
                String group = matcher.group("group");
                if (group == null) {
                    mainObservableFile.save();
                } else {
                    String prefix = group + ".";
                    boolean anyLeft = checkRulesByName.keySet().stream().anyMatch(s -> s.startsWith(prefix));

                    ObservableConfigFile observableConfigFile;
                    if (!extraObservableFiles.containsKey(group)) {
                        //or throw an exception here?
                        observableConfigFile = addExtraObservable(group, false);
                    } else {
                        observableConfigFile = extraObservableFiles.get(group);
                    }

                    if (anyLeft) {
                        observableConfigFile.save();
                    } else {
                        observableConfigFile.close();
                        Files.delete(observableConfigFile.getPath());
                    }
                }

                return CompletableFuture.completedFuture(Boolean.TRUE);
                // TODO: return CompletableFuture from forceSave
            } else {
                return CompletableFuture.completedFuture(Boolean.FALSE);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private Set<CheckRuleIndex> getIndex(CheckRule rule) {
        Set<String> ids = allKnownIds.stream().filter(rule.idIndexFilter()).collect(Collectors.toSet());
        if (ids.size() == 0) {
            CheckRuleIndex index = CheckRuleIndex.of(rule.getQueryNode());
            logger.warn("unable to find acceptable item for rule {} using {}.", rule.getName(), index);
            return Collections.singleton(index);
        } else if (ids.size() > 16) {
            return Collections.singleton(CheckRuleIndex.of());
        } else {
            return ids.stream().map(CheckRuleIndex::of).collect(Collectors.toSet());
        }
    }

    private void load(ConfigurationNode node, String group) throws IOException {
        String prefix = group.isEmpty() ? "" : group + ".";
        Predicate<String> nameChecker = name -> group.isEmpty() ? name.indexOf('.') == -1 : name.startsWith(prefix);
        Path path = group.isEmpty() ? mainPath : extraDir.resolve(group + ".conf");
        AtomicBoolean needSave = new AtomicBoolean(false);
        int version;
        if (node.getChildrenMap().isEmpty()) {
            version = CURRENT_VERSION;
            needSave.set(true);
        } else {
            version =
                node
                    .getNode("epicbanitem-version")
                    .<Integer>getValue(
                        Types::asInt,
                        () -> {
                            logger.warn("Ban Config at {} is missing epicbanitem-version option.", path);
                            logger.warn("Try loading using current version {}.", CURRENT_VERSION);
                            needSave.set(true);
                            return CURRENT_VERSION;
                        }
                    );
        }

        ObservableConfigFile observableConfigFile;
        if (group.isEmpty()) {
            observableConfigFile = mainObservableFile;
        } else {
            observableConfigFile = extraObservableFiles.get(group);
        }
        if (observableConfigFile == null) {
            throw new IllegalStateException("unable to find fit observableConfigFile on load.");
        }
        if (version < CURRENT_VERSION) {
            Path backup = observableConfigFile.backup();
            logger.warn("Find a version {} config, trying update it to {}. A copy is created at {}", version, CURRENT_VERSION, backup);
            node = updateService.update(UpdateService.BAN_CONF, node, version, CURRENT_VERSION);
            needSave.set(true);
        } else if (version > CURRENT_VERSION) {
            logger.warn("Find ban config {} with greater version {} than current {}, this file may not be load rightly", mainObservableFile.getPath(), version, CURRENT_VERSION);
            needSave.set(true);
        }

        SortedMap<String, CheckRule> byName = new TreeMap<>(RULE_NAME_COMPARATOR);
        ImmutableListMultimap.Builder<CheckRuleIndex, CheckRule> byItem = ImmutableListMultimap
            .<CheckRuleIndex, CheckRule>builder()
            .orderKeysBy(Comparator.comparing(Object::toString))
            .orderValuesBy(COMPARATOR);

        this.checkRulesByName.forEach(
                (name, rule) -> {
                    if (!nameChecker.test(name)) {
                        byName.put(name, rule);
                    }
                }
            );
        this.checkRulesByIndex.forEach(
                (index, rule) -> {
                    if (!nameChecker.test(rule.getName())) {
                        byItem.put(index, rule);
                    }
                }
            );

        Map<Object, ? extends ConfigurationNode> checkRules = node.getNode("epicbanitem").getChildrenMap();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : checkRules.entrySet()) {
            try {
                String name = prefix + entry.getKey().toString();
                CheckRule rule = Objects.requireNonNull(entry.getValue().getValue(RULE_BUILDER_TOKEN)).name(name).build();
                byName.put(name, rule);
                Set<CheckRuleIndex> checkRuleIndices = getIndex(rule);
                checkRuleIndices.forEach(index -> byItem.put(index, rule));
            } catch (ObjectMappingException e) {
                throw new IOException(e);
            }
        }
        this.checkRulesByIndex = byItem.build();
        this.checkRulesByName = ImmutableSortedMap.copyOfSorted(byName);
        this.cacheFromIdToCheckRules.invalidateAll();
        if (needSave.get()) {
            observableConfigFile.save();
        }
    }

    private void delete(String group) {
        String prefix = group + ".";
        Predicate<String> nameChecker = name -> name.startsWith(prefix);
        SortedMap<String, CheckRule> byName = new TreeMap<>(RULE_NAME_COMPARATOR);
        ImmutableListMultimap.Builder<CheckRuleIndex, CheckRule> byItem = ImmutableListMultimap
            .<CheckRuleIndex, CheckRule>builder()
            .orderKeysBy(Comparator.comparing(Object::toString))
            .orderValuesBy(COMPARATOR);

        this.checkRulesByName.forEach(
                (name, rule) -> {
                    if (!nameChecker.test(name)) {
                        byName.put(name, rule);
                    }
                }
            );
        this.checkRulesByIndex.forEach(
                (index, rule) -> {
                    if (!nameChecker.test(rule.getName())) {
                        byItem.put(index, rule);
                    }
                }
            );
        this.checkRulesByIndex = byItem.build();
        this.checkRulesByName = ImmutableSortedMap.copyOfSorted(byName);
        this.cacheFromIdToCheckRules.invalidateAll();
    }

    private void save(ConfigurationNode node, String group) throws IOException {
        String prefix = group.isEmpty() ? "" : group + ".";
        Function<String, Optional<String>> getConfigName = name -> {
            if (group.isEmpty()) {
                return name.indexOf('.') == -1 ? Optional.of(name) : Optional.empty();
            } else {
                return name.startsWith(prefix) ? Optional.of(name.substring(name.indexOf('.') + 1)) : Optional.empty();
            }
        };
        node.getNode("epicbanitem-version").setValue(CURRENT_VERSION);
        for (CheckRule checkRule : checkRulesByName.values()) {
            Optional<String> optionalName = getConfigName.apply(checkRule.getName());
            if (optionalName.isPresent()) {
                try {
                    node.getNode("epicbanitem", optionalName.get()).setValue(TypeToken.of(CheckRule.Builder.class), CheckRule.builder(checkRule));
                } catch (ObjectMappingException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    private ObservableConfigFile addExtraObservable(String group, boolean load) throws IOException {
        ObservableConfigFile observableConfigFile;
        observableConfigFile =
            ObservableConfigFile
                .builder()
                .path(extraDir.resolve(group + ".conf"))
                .updateConsumer(node -> this.load(node, group))
                .saveConsumer(node -> this.save(node, group))
                .deleteConsumer(node -> delete(group))
                .configDir(configDir)
                .build();
        extraObservableFiles.put(group, observableConfigFile);
        fileService.register(observableConfigFile);
        if (load) {
            observableConfigFile.load();
            observableConfigFile.save();
        }
        return observableConfigFile;
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) throws IOException {
        for (ItemType itemType : Sponge.getRegistry().getAllOf(ItemType.class)) {
            ItemStack itemStack = ItemStack.builder().itemType(itemType).build();
            String id = NbtTagDataUtil.getId(NbtTagDataUtil.toNbt(itemStack));
            allKnownIds.add(id);
        }
        //load at post init for id scan & trigger registry
        TypeSerializers.getDefaultSerializers().registerType(RULE_BUILDER_TOKEN, injector.getInstance(CheckRule.BuilderSerializer.class));
        if (Files.notExists(extraDir)) {
            Files.createDirectories(extraDir);
        }
        this.mainObservableFile =
            ObservableConfigFile
                .builder()
                .path(mainPath)
                .updateConsumer(node -> this.load(node, ""))
                .saveConsumer(node -> this.save(node, ""))
                .configDir(configDir)
                .build();
        fileService.register(mainObservableFile);
        this.mainObservableFile.load();
        this.mainObservableFile.save();


        Function<Path, Optional<String>> getGroupName = path -> {
            Matcher matcher = FILE_NAME_PATTERN.matcher(path.getFileName().toString());
            if (matcher.matches()) {
                return Optional.ofNullable(matcher.group(1));
            } else {
                return Optional.empty();
            }
        };

        for (Path path : Files.list(extraDir).collect(Collectors.toList())) {
            Optional<String> optionalGroup = getGroupName.apply(path);
            if (optionalGroup.isPresent()) {
                addExtraObservable(optionalGroup.get(), true);
            } else {
                logger.warn("Find unknown file {} in config dir, it will not be loaded.", path);
            }
        }

        fileService.register(
            ObservableDirectory
                .builder()
                .path(extraDir)
                .createConsumer(
                    path -> {
                        Path fileName = path.getFileName();
                        Optional<String> optionalGroup = getGroupName.apply(fileName);
                        if (optionalGroup.isPresent()) {
                            addExtraObservable(optionalGroup.get(), true);
                        } else {
                            if (fileName.endsWith("conf")) {
                                logger.warn("Find unknown file {} in config dir, it will not be loaded.", fileName);
                            }
                        }
                    }
                )
                .build()
        );
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        metrics.addCustomChart(new Metrics.SingleLineChart("enabledCheckRules", () -> this.getRules().size()));
    }
}
