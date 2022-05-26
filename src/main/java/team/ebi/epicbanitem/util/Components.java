/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Components {

    public static final Component NEED_PLAYER =
            Component.translatable("epicbanitem.command.needPlayer", NamedTextColor.RED);
    public static final Component NEED_BLOCK = Component.translatable("epicbanitem.command.needBlock");
    public static final Component NEED_ITEM = Component.translatable("epicbanitem.command.needItem");
    public static final TranslatableComponent INFO = Component.translatable("epicbanitem.ui.info")
            .hoverEvent(Component.translatable("epicbanitem.ui.info.description"));

    public static final TranslatableComponent TEST_HELD = Component.translatable("epicbanitem.ui.testHeld")
            .hoverEvent(Component.translatable("epicbanitem.ui.testHeld.description"));

    private Components() {}
}
