package team.ebi.epicbanitem.api;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import team.ebi.epicbanitem.EpicBanItem;

public abstract class AbstractRestrictionTrigger implements RestrictionTrigger {
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
}
