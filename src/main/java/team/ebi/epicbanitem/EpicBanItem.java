package team.ebi.epicbanitem;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Objects;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resource.pack.PackContents;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import team.ebi.epicbanitem.util.PropertyResourceBundle;

/**
 * @author The EpicBanItem Team
 */
@Plugin(EpicBanItem.NAMESPACE)
public class EpicBanItem {
  public static final String NAMESPACE = "epicbanitem";

  public static ResourceKey key(String value) {
    return ResourceKey.of(NAMESPACE, value);
  }

  public static String permission(String permission) {
    return NAMESPACE + "." + permission;
  }

  @Inject private PluginContainer plugin;
  @Inject private Injector injector;

  @Inject
  EpicBanItem(EBIRegistries registries) {
    Objects.requireNonNull(registries);
  }

  @Listener
  public void onRegisterCommand(RegisterCommandEvent<Command.Parameterized> event) {
    event.register(plugin, injector.getInstance(EBICommands.class).build(), "epicbanitem", "ebi");
  }

  @Listener
  public void onStartingEngine(final StartingEngineEvent<Server> event) {
    final TranslationRegistry translations = TranslationRegistry.create(key("translations"));
    try (Pack pack = Sponge.server().packRepository().pack(plugin)) {
      PackContents contents = pack.contents();
      Collection<ResourcePath> paths =
          contents.paths(
              PackType.server(),
              EpicBanItem.NAMESPACE,
              "assets/messages",
              3,
              name -> name.endsWith("properties"));
      for (ResourcePath path : paths) {
        String name = path.name();
        Resource resource = contents.requireResource(PackType.server(), path);
        String locale = name.substring(0, name.lastIndexOf(".properties"));
        PropertyResourceBundle bundle =
            new PropertyResourceBundle(new InputStreamReader(resource.inputStream()));
        translations.registerAll(Locales.of(locale), bundle, false);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    GlobalTranslator.translator().addSource(translations);
  }
}
