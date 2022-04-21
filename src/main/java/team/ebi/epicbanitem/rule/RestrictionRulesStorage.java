package team.ebi.epicbanitem.rule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.stream.Stream;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRules;

@Singleton
public class RestrictionRulesStorage {

  private final Path rulesDir;

  @Inject
  public RestrictionRulesStorage(
      PluginContainer plugin,
      EventManager eventManager,
      @ConfigDir(sharedRoot = false) Path configDir)
      throws IOException {
    eventManager.registerListeners(plugin, this);
    this.rulesDir = configDir.resolve("rules");
    this.rulesFromFiles().forEach(RestrictionRules.registry()::register);
  }

  @Listener
  public void onRefreshGame(RefreshGameEvent event) throws IOException {
    this.rulesFromFiles().forEach(RestrictionRules.registry()::register);
  }

  public void save() {
    RestrictionRules.registry()
        .streamEntries()
        .forEach(
            entry -> {
              try {
                HoconConfigurationLoader loader =
                    HoconConfigurationLoader.builder()
                        .path(rulesDir.resolve(entry.key().value()))
                        .build();
                loader.save(loader.createNode().set(entry.value().toContainer()));
              } catch (ConfigurateException e) {
                throw new RuntimeException(e);
              }
            });
  }

  private ImmutableMap<ResourceKey, RestrictionRule> rulesFromFiles() throws IOException {

    try (Stream<Path> paths =
        Files.find(
            rulesDir,
            2,
            (path, attributes) ->
                attributes.isRegularFile() && path.toString().endsWith(".conf"))) {
      return paths.collect(
          ImmutableMap.toImmutableMap(
              it -> EpicBanItem.key(getNameWithoutExtension(it.toString())),
              it -> {
                try {
                  return Objects.requireNonNull(
                          HoconConfigurationLoader.builder()
                              .path(it)
                              .build()
                              .load()
                              .get(DataContainer.class))
                      .getObject(DataQuery.of(), RestrictionRuleImpl.class)
                      .orElseThrow(
                          () ->
                              new InvalidDataException(
                                  MessageFormat.format("Rule file {} can't parse", it)));
                } catch (ConfigurateException e) {
                  throw new RuntimeException(e);
                }
              }));
    }
  }

  private String getNameWithoutExtension(String name) {
    int dotIndex = name.lastIndexOf('.');
    return (dotIndex == -1) ? name : name.substring(0, dotIndex);
  }
}
