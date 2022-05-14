package team.ebi.epicbanitem;

import java.text.MessageFormat;
import java.util.Locale;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.TranslationRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EBITranslationRegistry implements TranslationRegistry {

  private final TranslationRegistry registry;

  public EBITranslationRegistry() {
    this.registry = TranslationRegistry.create(EpicBanItem.key("translations"));
  }

  @Override
  @NotNull
  public Key name() {
    return registry.name();
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
}
