package team.ebi.epicbanitem.configuration;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
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
import team.ebi.epicbanitem.api.CheckRuleIndex;
import team.ebi.epicbanitem.api.CheckRuleLocation;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.configuration.update.UpdateService;
import team.ebi.epicbanitem.util.NbtTagDataUtil;
import team.ebi.epicbanitem.util.file.ObservableDirectory;
import team.ebi.epicbanitem.util.file.ObservableFileService;
import team.ebi.epicbanitem.util.repackage.org.bstats.sponge.Metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author The EpicBanItem Team
 */
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
    private static final Comparator<CheckRule> COMPARATOR = CheckRule.getDefaultComparator();

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

    private final Set<String> allKnownIds = new LinkedHashSet<>();
    private final Map<String, ObservableConfigFile> extraObservableFiles;

    private ImmutableSortedMap<CheckRuleLocation, CheckRule> checkRulesByName = ImmutableSortedMap.of();
    private ImmutableListMultimap<CheckRuleIndex, CheckRule> checkRulesByIndex = ImmutableListMultimap.of();

    @Inject
    private BanConfig(@ConfigDir(sharedRoot = false) Path configDir, EventManager eventManager, PluginContainer pluginContainer) {
        this.mainPath = configDir.resolve(MAIN_CONFIG_PATH);
        this.extraDir = configDir.resolve(EXTRA_CONFIG_DIR).toAbsolutePath();
        this.configDir = configDir;
        this.extraObservableFiles = Maps.newHashMap();
        eventManager.registerListeners(pluginContainer, this);
    }

    public Iterable<? extends CheckRule> getRulesWithIdFiltered(String id) {
        CheckRuleIndex wildcard = CheckRuleIndex.of(), withId = CheckRuleIndex.of(id);
        return Iterables.mergeSorted(Arrays.asList(getRules(wildcard), getRules(withId)), COMPARATOR);
    }

    public List<CheckRule> getRules(CheckRuleIndex index) {
        return checkRulesByIndex.get(index);
    }

    public Collection<CheckRule> getRules() {
        return checkRulesByName.values();
    }

    public Set<CheckRuleLocation> getRuleNames() {
        return checkRulesByName.keySet();
    }

    public Set<CheckRuleIndex> getItems() {
        return checkRulesByIndex.keySet();
    }

    public Optional<CheckRule> getRule(CheckRuleLocation name) {
        return Optional.ofNullable(checkRulesByName.get(name));
    }

    public CompletableFuture<Boolean> addRule(CheckRule newRule) throws IOException {
        try {
            SortedMap<CheckRuleLocation, CheckRule> rulesByName = new TreeMap<>(checkRulesByName);

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

            CheckRuleLocation name = newRule.getName();
            Optional<String> groupOptional = name.getLocationGroup();
            if (!groupOptional.isPresent()) {
                mainObservableFile.save();
            } else {
                String group = groupOptional.get();
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

    public CompletableFuture<Boolean> removeRule(CheckRuleLocation name) throws IOException {
        try {
            CheckRule rule = checkRulesByName.get(name);
            if (rule != null) {
                SortedMap<CheckRuleLocation, CheckRule> rulesByName = new TreeMap<>(checkRulesByName);
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
                Optional<String> groupOptional = name.getLocationGroup();

                if (!groupOptional.isPresent()) {
                    mainObservableFile.save();
                } else {
                    String group = groupOptional.get();
                    ObservableConfigFile observableConfigFile;
                    if (!extraObservableFiles.containsKey(group)) {
                        //or throw an exception here?
                        observableConfigFile = addExtraObservable(group, false);
                    } else {
                        observableConfigFile = extraObservableFiles.get(group);
                    }

                    if (checkRulesByName.keySet().stream().anyMatch(l -> groupOptional.equals(l.getLocationGroup()))) {
                        observableConfigFile.save();
                    } else {
                        observableConfigFile.close();
                        extraObservableFiles.remove(group);
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
            logger.warn("unable to find acceptable item for rule {} using {}.", rule.getName().toString(), index);
            return Collections.singleton(index);
        } else if (ids.size() > 16) {
            return Collections.singleton(CheckRuleIndex.of());
        } else {
            return ids.stream().map(CheckRuleIndex::of).collect(Collectors.toSet());
        }
    }

    private void load(ConfigurationNode node, Optional<String> group) throws IOException {
        Path path = group.map(s -> extraDir.resolve(s + ".conf")).orElse(mainPath);
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
        if (!group.isPresent()) {
            observableConfigFile = mainObservableFile;
        } else {
            observableConfigFile = extraObservableFiles.get(group.get());
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

        SortedMap<CheckRuleLocation, CheckRule> byName = new TreeMap<>();
        ImmutableListMultimap.Builder<CheckRuleIndex, CheckRule> byItem = ImmutableListMultimap
            .<CheckRuleIndex, CheckRule>builder()
            .orderKeysBy(Comparator.comparing(Object::toString))
            .orderValuesBy(COMPARATOR);

        this.checkRulesByName.forEach((name, rule) -> {
            if (!name.getLocationGroup().equals(group)) {
                byName.put(name, rule);
            }
        });
        this.checkRulesByIndex.forEach((index, rule) -> {
            if (!rule.getName().getLocationGroup().equals(group)) {
                byItem.put(index, rule);
            }
        });

        Map<Object, ? extends ConfigurationNode> checkRules = node.getNode("epicbanitem").getChildrenMap();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : checkRules.entrySet()) {
            try {
                String name = group.map(s -> s + ".").orElse("") + entry.getKey().toString();
                CheckRuleLocation location = CheckRuleLocation.of(name);
                CheckRule.Builder builder = entry.getValue().getValue(RULE_BUILDER_TOKEN);
                CheckRule rule = Objects.requireNonNull(builder).name(location).build();
                byName.put(location, rule);
                Set<CheckRuleIndex> checkRuleIndices = getIndex(rule);
                checkRuleIndices.forEach(index -> byItem.put(index, rule));
            } catch (ObjectMappingException e) {
                throw new IOException(e);
            }
        }
        this.checkRulesByIndex = byItem.build();
        this.checkRulesByName = ImmutableSortedMap.copyOfSorted(byName);
        if (needSave.get()) {
            observableConfigFile.save();
        }
    }

    private void delete(Optional<String> group) {
        SortedMap<CheckRuleLocation, CheckRule> byName = new TreeMap<>();
        ImmutableListMultimap.Builder<CheckRuleIndex, CheckRule> byItem = ImmutableListMultimap
            .<CheckRuleIndex, CheckRule>builder()
            .orderKeysBy(Comparator.comparing(Object::toString))
            .orderValuesBy(COMPARATOR);

        this.checkRulesByName.forEach((name, rule) -> {
            if (!name.getLocationGroup().equals(group)) {
                byName.put(name, rule);
            }
        });
        this.checkRulesByIndex.forEach((index, rule) -> {
            if (!rule.getName().getLocationGroup().equals(group)) {
                byItem.put(index, rule);
            }
        });

        this.checkRulesByIndex = byItem.build();
        this.checkRulesByName = ImmutableSortedMap.copyOfSorted(byName);
    }

    private void save(ConfigurationNode node, Optional<String> group) throws IOException {
        node.getNode("epicbanitem-version").setValue(CURRENT_VERSION);
        for (CheckRule checkRule : checkRulesByName.values()) {
            CheckRuleLocation location = checkRule.getName();
            if (location.getLocationGroup().equals(group)) {
                try {
                    ConfigurationNode childNode = node.getNode("epicbanitem", location.getLocationName());
                    childNode.setValue(TypeToken.of(CheckRule.Builder.class), CheckRule.builder(checkRule));
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
                .updateConsumer(node -> this.load(node, Optional.of(group)))
                .saveConsumer(node -> this.save(node, Optional.of(group)))
                .deleteConsumer(node -> delete(Optional.of(group)))
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
                .updateConsumer(node -> this.load(node, Optional.empty()))
                .saveConsumer(node -> this.save(node, Optional.empty()))
                .deleteConsumer(node -> delete(Optional.empty()))
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
