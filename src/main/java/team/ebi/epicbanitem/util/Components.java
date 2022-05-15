package team.ebi.epicbanitem.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Components {

  public static final Component NEED_PLAYER = Component.translatable(
      "epicbanitem.command.needPlayer", NamedTextColor.RED);
  public static final Component NEED_BLOCK = Component.translatable(
      "epicbanitem.command.needBlock");
  public static final Component NEED_ITEM = Component.translatable("epicbanitem.command.needItem");
  public static final TranslatableComponent EDIT =
      Component.translatable("epicbanitem.ui.edit")
          .hoverEvent(Component.translatable("epicbanitem.ui.edit.description"));

  public static final TranslatableComponent TEST_HELD =
      Component.translatable("epicbanitem.ui.testHeld")
          .hoverEvent(Component.translatable("epicbanitem.ui.testHeld.description"));

  private Components() {
  }
}
