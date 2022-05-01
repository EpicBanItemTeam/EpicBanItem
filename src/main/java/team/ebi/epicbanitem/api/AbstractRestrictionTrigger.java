package team.ebi.epicbanitem.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import team.ebi.epicbanitem.EBITranslator;
import team.ebi.epicbanitem.EpicBanItem;

public abstract class AbstractRestrictionTrigger implements RestrictionTrigger {
  protected final EBITranslator translator = EpicBanItem.translator();

  private final ResourceKey key;

  public AbstractRestrictionTrigger(ResourceKey key) {
    this.key = key;
    Sponge.eventManager()
        .registerListeners(
            Sponge.pluginManager()
                .plugin(EpicBanItem.NAMESPACE)
                .orElseThrow(() -> new IllegalStateException("EpicBanItem haven't been loaded")),
            this);
  }

  @Override
  public @NotNull ResourceKey key() {
    return key;
  }

  @Override
  public @NotNull Component asComponent() {
    return Component.translatable("trigger." + key());
  }

  @Override
  public Component description() {
    return Component.translatable("trigger." + key() + ".description");
  }
}
