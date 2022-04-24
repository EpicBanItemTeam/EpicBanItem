package team.ebi.epicbanitem.api;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryKey;
import team.ebi.epicbanitem.EpicBanItem;

public abstract class AbstractRestrictionTrigger implements RestrictionTrigger {
  private final ResourceKey key;

  public AbstractRestrictionTrigger(RegistryKey<RestrictionTrigger> key) {
    this.key = key.location();
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
