package team.ebi.epicbanitem.api;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;

public class RestrictionPresets {

  public static final DefaultedRegistryReference<RestrictionPreset> TYPE =
      key(EpicBanItem.key("type"));

  public static final DefaultedRegistryReference<RestrictionPreset> ALL =
      key(EpicBanItem.key("all"));

  public static final DefaultedRegistryReference<RestrictionPreset> EMPTY =
      key(EpicBanItem.key("empty"));

  public static Registry<RestrictionPreset> registry() {
    return Sponge.server().registry(EBIRegistries.PRESET);
  }

  private static DefaultedRegistryReference<RestrictionPreset> key(final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.PRESET, location).asDefaultedReference(Sponge::server);
  }
}
