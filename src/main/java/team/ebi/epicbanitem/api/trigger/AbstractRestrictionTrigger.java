/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.trigger;

import java.util.Locale;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.util.locale.LocaleSource;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.EpicBanItem;

public abstract class AbstractRestrictionTrigger implements RestrictionTrigger {

    private final ResourceKey key;

    protected AbstractRestrictionTrigger(ResourceKey key) {
        this.key = key;
        Sponge.eventManager()
                .registerListeners(
                        Sponge.pluginManager()
                                .plugin(EpicBanItem.NAMESPACE)
                                .orElseThrow(() -> new IllegalStateException("EpicBanItem haven't been loaded")),
                        this);
    }

    @Override
    public @NotNull ResourceKey key() {
        return key;
    }

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

    protected Locale locale(Cause cause) {
        return cause.last(LocaleSource.class).map(LocaleSource::locale).orElse(Locale.getDefault());
    }
}
