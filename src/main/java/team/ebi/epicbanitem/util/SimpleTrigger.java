package team.ebi.epicbanitem.util;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.Trigger;

public class SimpleTrigger implements Trigger {
  private final Component component;

  public SimpleTrigger(String name) {
    this.component = Component.translatable("trigger." + name);
  }

  @Override
  public @NotNull Component asComponent() {
    return component;
  }
}
