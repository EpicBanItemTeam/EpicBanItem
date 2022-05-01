package team.ebi.epicbanitem.api;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.registry.RegistryScopes;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.trigger.SimpleRestrictionTrigger;
import team.ebi.epicbanitem.trigger.UseRestrictionTrigger;

@RegistryScopes(scopes = RegistryScope.ENGINE)
public class RestrictionTriggers {
  public static final DefaultedRegistryReference<RestrictionTrigger> USE =
      key(EpicBanItem.key("use"));
  public static final DefaultedRegistryReference<RestrictionTrigger> INTERACT =
      key(EpicBanItem.key("interact"));

  public static final DefaultedRegistryReference<RestrictionTrigger> EQUIP =
      key(EpicBanItem.key("equip"));
  public static final DefaultedRegistryReference<RestrictionTrigger> CRAFT =
      key(EpicBanItem.key("craft"));
  public static final DefaultedRegistryReference<RestrictionTrigger> STORE =
      key(EpicBanItem.key("store"));
  public static final DefaultedRegistryReference<RestrictionTrigger> CLICK =
      key(EpicBanItem.key("click"));

  public static final DefaultedRegistryReference<RestrictionTrigger> PICKUP =
      key(EpicBanItem.key("pickup"));
  public static final DefaultedRegistryReference<RestrictionTrigger> THROW =
      key(EpicBanItem.key("throw"));
  public static final DefaultedRegistryReference<RestrictionTrigger> DROP =
      key(EpicBanItem.key("drop"));

  public static final DefaultedRegistryReference<RestrictionTrigger> PLACE =
      key(EpicBanItem.key("place"));
  public static final DefaultedRegistryReference<RestrictionTrigger> BREAK =
      key(EpicBanItem.key("break"));

  public static final DefaultedRegistryReference<RestrictionTrigger> JOIN =
      key(EpicBanItem.key("join"));

  public static Map<ResourceKey, RestrictionTrigger> DEFAULT_REGISTRIES =
      ImmutableMap.<ResourceKey, RestrictionTrigger>builder()
          .put(EpicBanItem.key("use"), new UseRestrictionTrigger())
          .put(EpicBanItem.key("equip"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("craft"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("pickup"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("click"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("throw"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("drop"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("place"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("break"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("interact"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("join"), new SimpleRestrictionTrigger())
          .put(EpicBanItem.key("store"), new SimpleRestrictionTrigger())
          .build();

  public static Registry<RestrictionTrigger> registry() {
    return Sponge.server().registry(EBIRegistries.TRIGGER);
  }

  private static DefaultedRegistryReference<RestrictionTrigger> key(final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.TRIGGER, location).asDefaultedReference(Sponge::server);
  }
}
