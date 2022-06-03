/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.rule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.RestrictionRuleQueries;
import team.ebi.epicbanitem.api.rule.RestrictionRuleService;

@Singleton
public class RestrictionRulesStorage {

    private final Path rulesDir;
    private final HoconConfigurationLoader.Builder configBuilder;
    private final RestrictionRuleService ruleService;

    @Inject
    public RestrictionRulesStorage(
            EventManager eventManager, ConfigManager configManager, RestrictionRuleService ruleService)
            throws IOException {
        PluginContainer plugin =
                Sponge.pluginManager().plugin(EpicBanItem.NAMESPACE).orElseThrow();
        eventManager.registerListeners(plugin, this);
        this.rulesDir = configManager.pluginConfig(plugin).directory().resolve("rules");
        this.configBuilder = HoconConfigurationLoader.builder()
                .defaultOptions(ConfigurationOptions.defaults().serializers(configManager.serializers()));
        this.ruleService = ruleService;
        if (Files.notExists(this.rulesDir)) {
            Files.createDirectories(this.rulesDir);
        }
        this.load();
    }

    @Listener
    public void onRefreshGame(RefreshGameEvent event) throws IOException {
        ruleService.clear();
        this.load();
    }

    private void load() throws IOException {
        this.rulesFromFiles().forEach((key, rule) -> ruleService
                .register(key, rule)
                .orElseThrow(
                        () -> new IllegalStateException(MessageFormat.format("Rule {0} can't parse", key.value()))));
    }

    public void remove(ResourceKey key) {
        ruleService.remove(key);
        try {
            Files.delete(rulesDir.resolve(configExtension(key.value())));
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void save(ResourceKey key) {
        ruleService.of(key).ifPresent(it -> save(key, it));
    }

    public void save(ResourceKey key, RestrictionRule rule) {
        try {
            final var loader = this.configBuilder
                    .path(rulesDir.resolve(configExtension(key.value())))
                    .build();
            loader.save(loader.createNode().set(rule));
        } catch (ConfigurateException e) {
            throw new IllegalStateException(e);
        }
    }

    public void save() {
        ruleService.all().forEach((key, rule) -> {
            if (key.namespace().equals(EpicBanItem.NAMESPACE)) {
                this.save(key, rule);
            }
        });
    }

    private String configExtension(String name) {
        return name + ".conf";
    }

    private Map<ResourceKey, RestrictionRule> rulesFromFiles() throws IOException {
        final var keyQuery = RestrictionRuleQueries.RULE.then(RestrictionRuleQueries.KEY);
        try (final var paths = Files.find(
                rulesDir,
                2,
                (path, attributes) -> attributes.isRegularFile()
                        && path.toString().endsWith(".conf")
                        && !path.getFileName().toString().equals(".conf"))) {
            return paths.collect(Collectors.toUnmodifiableMap(
                    it -> EpicBanItem.key(
                            getNameWithoutExtension(it.getFileName().toString())),
                    it -> {
                        try {
                            // Need a way to translate ndoe to data view
                            //                            final var view =
                            // configBuilder.path(it).build().load().get(DataContainer.class);
                            //                            if (!Objects.requireNonNull(view).contains(keyQuery))
                            //                                view.set(keyQuery, EpicBanItem.key(
                            //
                            // getNameWithoutExtension(it.getFileName().toString())));
                            //                            return
                            // Objects.requireNonNull(Sponge.dataManager().deserialize(RestrictionRule.class,
                            // view).orElse(null));
                            return Objects.requireNonNull(
                                    configBuilder.path(it).build().load().get(RestrictionRule.class));
                        } catch (ConfigurateException e) {
                            throw new IllegalStateException(e);
                        }
                    }));
        }
    }

    private String getNameWithoutExtension(String name) {
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex == -1) ? name : name.substring(0, dotIndex);
    }
}
