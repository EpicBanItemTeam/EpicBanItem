package team.ebi.epicbanitem.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;
import team.ebi.epicbanitem.EpicBanItem;

/** Recommend to extend {@link AbstractRestrictionTrigger} */
@CatalogedBy(RestrictionTriggers.class)
public interface RestrictionTrigger extends DefaultedRegistryValue, ResourceKeyed, ComponentLike {
  String CONTEXT_KEY = EpicBanItem.NAMESPACE + "-trigger";

  Component description();
}
