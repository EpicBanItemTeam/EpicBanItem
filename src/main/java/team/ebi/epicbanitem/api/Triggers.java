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
          .put(EpicBanItem.key("use"), new SimpleTrigger("use"))
          .put(EpicBanItem.key("equip"), new SimpleTrigger("equip"))
          .put(EpicBanItem.key("craft"), new SimpleTrigger("craft"))
          .put(EpicBanItem.key("pickup"), new SimpleTrigger("pickup"))
          .put(EpicBanItem.key("click"), new SimpleTrigger("click"))
          .put(EpicBanItem.key("throw"), new SimpleTrigger("throw"))
          .put(EpicBanItem.key("drop"), new SimpleTrigger("drop"))
          .put(EpicBanItem.key("place"), new SimpleTrigger("place"))
          .put(EpicBanItem.key("break"), new SimpleTrigger("break"))
          .put(EpicBanItem.key("interact"), new SimpleTrigger("interact"))
          .put(EpicBanItem.key("join"), new SimpleTrigger("join"))
          .put(EpicBanItem.key("store"), new SimpleTrigger("store"))
          .build();

  public static Registry<Trigger> registry() {
    return Sponge.server().registry(EBIRegistries.TRIGGER);
  }

  private static DefaultedRegistryReference<Trigger> key(final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.TRIGGER, location).asDefaultedReference(Sponge::server);
  }
}
