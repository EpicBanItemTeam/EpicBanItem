package team.ebi.epicbanitem.api;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.trigger.SimpleTrigger;

public class Triggers {
  public static final DefaultedRegistryReference<Trigger> USE = key(EpicBanItem.key("use"));
  public static final DefaultedRegistryReference<Trigger> INTERACT =
      key(EpicBanItem.key("interact"));

  public static final DefaultedRegistryReference<Trigger> EQUIP = key(EpicBanItem.key("equip"));
  public static final DefaultedRegistryReference<Trigger> CRAFT = key(EpicBanItem.key("craft"));
  public static final DefaultedRegistryReference<Trigger> STORE = key(EpicBanItem.key("store"));
  public static final DefaultedRegistryReference<Trigger> CLICK = key(EpicBanItem.key("click"));

  public static final DefaultedRegistryReference<Trigger> PICKUP = key(EpicBanItem.key("pickup"));
  public static final DefaultedRegistryReference<Trigger> THROW = key(EpicBanItem.key("throw"));
  public static final DefaultedRegistryReference<Trigger> DROP = key(EpicBanItem.key("drop"));

  public static final DefaultedRegistryReference<Trigger> PLACE = key(EpicBanItem.key("place"));
  public static final DefaultedRegistryReference<Trigger> BREAK = key(EpicBanItem.key("break"));

  public static final DefaultedRegistryReference<Trigger> JOIN = key(EpicBanItem.key("join"));

  public static Map<ResourceKey, Trigger> DEFAULT_REGISTRIES =
      ImmutableMap.<ResourceKey, Trigger>builder()
          .put(EpicBanItem.key("use"), new SimpleTrigger())
          .put(EpicBanItem.key("equip"), new SimpleTrigger())
          .put(EpicBanItem.key("craft"), new SimpleTrigger())
          .put(EpicBanItem.key("pickup"), new SimpleTrigger())
          .put(EpicBanItem.key("click"), new SimpleTrigger())
          .put(EpicBanItem.key("throw"), new SimpleTrigger())
          .put(EpicBanItem.key("drop"), new SimpleTrigger())
          .put(EpicBanItem.key("place"), new SimpleTrigger())
          .put(EpicBanItem.key("break"), new SimpleTrigger())
          .put(EpicBanItem.key("interact"), new SimpleTrigger())
          .put(EpicBanItem.key("join"), new SimpleTrigger())
          .put(EpicBanItem.key("store"), new SimpleTrigger())
          .build();

  public static Registry<Trigger> registry() {
    return Sponge.server().registry(EBIRegistries.TRIGGER);
  }

  private static DefaultedRegistryReference<Trigger> key(final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.TRIGGER, location).asDefaultedReference(Sponge::server);
  }
}
