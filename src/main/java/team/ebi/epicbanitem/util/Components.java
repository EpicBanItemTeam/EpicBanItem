package team.ebi.epicbanitem.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public final class Components {
  public static final TranslatableComponent EDIT =
      Component.translatable("epicbanitem.ui.edit")
          .hoverEvent(Component.translatable("epicbanitem.ui.edit.description"));

  public static final TranslatableComponent TEST_HELD =
      Component.translatable("epicbanitem.ui.testHeld")
          .hoverEvent(Component.translatable("epicbanitem.ui.testHeld.description"));
}
