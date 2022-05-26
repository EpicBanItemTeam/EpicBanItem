/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.ResourceKey;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.RestrictionTrigger;

public class RestrictionTriggerImpl implements RestrictionTrigger {

    @Override
    public @NotNull Component asComponent() {
        final var resourceKey = key();
        final var key = EpicBanItem.NAMESPACE + ".trigger." + resourceKey;
        if (!EpicBanItem.translations.contains(key)) {
            return Component.text(resourceKey.asString());
        }
        return Component.translatable(key);
    }

    @Override
    public Component description() {
        return Component.translatable(EpicBanItem.NAMESPACE + ".trigger." + key() + ".description");
    }

    @Override
    public @NotNull ResourceKey key() {
        return key(EBIRegistries.TRIGGER);
    }
}
