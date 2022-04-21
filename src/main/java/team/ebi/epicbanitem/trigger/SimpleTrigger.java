package team.ebi.epicbanitem.trigger;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.api.Trigger;

public class SimpleTrigger implements Trigger {

  public SimpleTrigger() {}

  @Override
  public @NotNull Component asComponent() {
    return Component.translatable("trigger." + key() + ".name");
  }

  @Override
  public Component descriptionComponent() {
    return Component.translatable("trigger." + key() + ".description");
  }

  public ResourceKey key() {
    return key(EBIRegistries.TRIGGER);
  }
}
