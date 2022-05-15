package team.ebi.epicbanitem.trigger;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.api.RestrictionTrigger;

public class SimpleRestrictionTrigger implements RestrictionTrigger {

  @Override
  public @NotNull Component asComponent() {
    return Component.translatable("trigger." + key() + ".name");
  }

  @Override
  public Component description() {
    return Component.translatable("trigger." + key() + ".description");
  }

  @Override
  public @NotNull ResourceKey key() {
    return key(EBIRegistries.TRIGGER);
  }
}
