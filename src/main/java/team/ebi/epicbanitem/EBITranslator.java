package team.ebi.epicbanitem;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.translation.TranslationRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resource.pack.PackContents;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;

@Singleton
public class EBITranslator implements TranslationRegistry {

  private final TranslationRegistry registry;
  private final TranslatableComponentRenderer<Locale> renderer;
  private final PluginContainer plugin;

  @Inject
  public EBITranslator(PluginContainer plugin, EventManager eventManager) {
    this.registry = TranslationRegistry.create(EpicBanItem.key("translator"));
    this.renderer = TranslatableComponentRenderer.usingTranslationSource(this);
    this.plugin = plugin;
    // TODO Custom messages file & reload
    eventManager.registerListeners(plugin, this);
  }

  @Listener
  public void onStartedEngine(StartedEngineEvent<Server> event) {
    try (Pack pack = Sponge.server().packRepository().pack(plugin)) {
      PackContents contents = pack.contents();
      Collection<ResourcePath> paths =
          contents.paths(
              PackType.server(), EpicBanItem.NAMESPACE, "lang", 1, name -> name.endsWith("lang"));
      for (ResourcePath path : paths) {
        String name = path.name();
        Resource resource = contents.requireResource(PackType.server(), path);
        String locale = name.substring(0, name.lastIndexOf(".lang"));
        PropertyResourceBundle bundle =
            new PropertyResourceBundle(new InputStreamReader(resource.inputStream()));
        this.registerAll(Locales.of(locale), bundle, false);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Component render(@NotNull Component component, Locale locale) {
    return renderer.render(component, locale);
  }

  @Override
  public boolean contains(@NotNull String key) {
    return registry.contains(key);
  }

  @Override
  @Nullable
  public MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
    return registry.translate(key, locale);
  }

  @Override
  public void defaultLocale(@NotNull Locale locale) {
    registry.defaultLocale(locale);
  }

  @Override
  public void register(@NotNull String key, @NotNull Locale locale, @NotNull MessageFormat format) {
    registry.register(key, locale, format);
  }

  @Override
  public void unregister(@NotNull String key) {
    registry.unregister(key);
  }

  @Override
  @NotNull
  public Key name() {
    return registry.name();
  }

  private static final class PropertyResourceBundle extends java.util.PropertyResourceBundle {

    public PropertyResourceBundle(Reader reader) throws IOException {
      super(reader);
    }

    @Override
    public void setParent(ResourceBundle parent) {
      super.setParent(parent);
    }
  }
}
