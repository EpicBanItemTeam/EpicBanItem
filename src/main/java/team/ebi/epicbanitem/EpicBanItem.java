/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem;

import java.util.Objects;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author The EpicBanItem Team
 */
@Plugin(EpicBanItem.NAMESPACE)
public class EpicBanItem {

    public static final String NAMESPACE = "epicbanitem";

    private final PluginContainer plugin;

    private final Injector injector;

    @Inject
    EpicBanItem(
            final EBIRegistries registries,
            final EBITranslation translation,
            final PluginContainer plugin,
            final Injector injector) {
        this.plugin = plugin;
        this.injector = injector;
        Objects.requireNonNull(registries);
        Objects.requireNonNull(translation);
    }

    public static ResourceKey key(String value) {
        return ResourceKey.of(NAMESPACE, value);
    }

    public static String permission(String permission) {
        return NAMESPACE + "." + permission;
    }

    @Listener
    public void onRegisterCommand(RegisterCommandEvent<Command.Parameterized> event) {
        event.register(plugin, injector.getInstance(EBICommands.class).buildCommand(), NAMESPACE, "ebi");
    }
}
