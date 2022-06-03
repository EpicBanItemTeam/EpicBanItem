/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.ResourceKey;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.EBITranslation;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public abstract class EBIRestrictionTrigger extends AbstractRestrictionTrigger {
    @Inject
    private EBITranslation translation;

    protected EBIRestrictionTrigger(ResourceKey key) {
        super(key);
    }

    @Override
    public @NotNull Component asComponent() {
        final var resourceKey = key();
        final var key = EpicBanItem.NAMESPACE + ".trigger." + resourceKey;
        Component component = Component.text(resourceKey.asString());
        if (translation.registry.contains(key)) {
            component = Component.translatable(key);
        }
        return component.hoverEvent(description());
    }
}
