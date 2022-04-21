package team.ebi.epicbanitem.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

@CatalogedBy(Triggers.class)
public interface Trigger extends DefaultedRegistryValue, ComponentLike {
  Component descriptionComponent();
}
