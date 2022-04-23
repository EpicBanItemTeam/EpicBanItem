package team.ebi.epicbanitem;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.world.server.ServerWorld;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionTrigger;

public final class EBIEventContextKeys {
  public static final EventContextKey<RestrictionRule> RESTRICTION_RULE =
      EBIEventContextKeys.key(EpicBanItem.key("restriction_rule"), RestrictionRule.class);

  public static final EventContextKey<ServerWorld> OBJECT_RESTRICT =
      EBIEventContextKeys.key(EpicBanItem.key("object_restrict"), ServerWorld.class);

  public static final EventContextKey<RestrictionTrigger> RESTRICTION_TRIGGER =
      EBIEventContextKeys.key(EpicBanItem.key("trigger"), RestrictionTrigger.class);

  public static final EventContextKey<DataSerializable> RESTRICTED_OBJECT =
      EBIEventContextKeys.key(EpicBanItem.key("restricted_object"), DataSerializable.class);

  private EBIEventContextKeys() {}

  private static <T> EventContextKey<T> key(final ResourceKey location, final TypeToken<T> token) {
    return EventContextKey.builder().key(location).type(token).build();
  }

  private static <T> EventContextKey<T> key(final ResourceKey location, final Class<T> token) {
    return EventContextKey.builder().key(location).type(token).build();
  }
}
